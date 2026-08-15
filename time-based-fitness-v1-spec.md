# Time-Based Fitness — v1 Spec

Stack: Native Kotlin + Jetpack Compose, Room, DataStore, Hilt.
Philosophy: no AI, no calendar, no planning. App shows the right routine card based on what time it is right now. Open → Follow → Done.

---

## 1. What v1 Provides

- User picks which categories they want help with (subset of: Morning, Meals, Workout, Evening) and a time window for each.
- On app open, Home shows a card (or stacked cards) for whichever category window(s) match the current time.
- Tapping a card opens a static checklist/routine for that category.
- Marking a routine "Done" logs completion for that day and updates a simple streak counter.
- Settings screen lets the user edit their category selection and time windows later.

**Explicitly out of v1:** exercise library/database, AI, push notifications, nutrition macros, user-editable routine content, charts/history views (streak count only).

---

## 2. Screens

1. **Onboarding** (first launch only — gated by a DataStore boolean `hasOnboarded`)
   - Multi-select chips: Morning / Meals / Workout / Evening
   - For each selected category, a start-time and end-time picker
   - "Get Started" button → writes selections to Room, sets `hasOnboarded = true` → navigates to Home

2. **Home**
   - Reads current time, compares against enabled category windows
   - Shows one card per matching category, stacked and ordered by window start time
   - If nothing matches: empty state showing the next upcoming category + its start time
   - Shows current streak count (small, non-intrusive — e.g. top corner)

3. **Routine Detail**
   - Static checklist for the selected category (title + steps, loaded from bundled JSON asset)
   - Checkboxes per step (local UI state only, not persisted per-step in v1)
   - "Done" button at the bottom → writes a `CompletionLog` entry for (today, categoryId) → returns to Home, streak updates

4. **Settings**
   - Same UI as onboarding (category multi-select + time windows), pre-filled with current selections
   - Save → updates Room, returns to Home

---

## 3. State Flow

```
App Launch
   │
   ▼
Check DataStore: hasOnboarded?
   │
   ├── false → Onboarding screen
   │              │ (save selections, set hasOnboarded=true)
   │              ▼
   └── true  → Home screen
                  │
                  │  WindowMatcher(now, enabledSelections) → matchingCategories[]
                  │
                  ├── matchingCategories not empty → show stacked cards (ordered by startTime)
                  └── matchingCategories empty      → show "next up" empty state
                  │
                  │  (tap a card)
                  ▼
              Routine Detail screen
                  │  (tap Done)
                  │  → write CompletionLog(date=today, categoryId)
                  │  → recompute streak
                  ▼
              back to Home (state refreshed)

Settings is reachable from Home at any time; saving returns to Home with fresh state.
```

Home's state is a simple derived/computed state, not something stored — it's recomputed every time Home is composed (on launch, on resume, on returning from Routine Detail or Settings) by re-running `WindowMatcher` against current time + current selections from Room.

---

## 4. Data Model (Room)

```kotlin
enum class Category { MORNING, MEALS, WORKOUT, EVENING }

@Entity
data class CategorySelection(
    @PrimaryKey val category: Category,
    val isEnabled: Boolean,
    val startTime: LocalTime,   // stored as minutes-since-midnight Int, converted via TypeConverter
    val endTime: LocalTime
)

@Entity(primaryKeys = ["date", "category"])
data class CompletionLog(
    val date: LocalDate,       // stored as epoch day Int
    val category: Category,
    val completedAt: Instant   // epoch millis
)
```

Static content (NOT in Room — bundled as a JSON asset, e.g. `assets/routines.json`):
```json
{
  "MORNING": { "title": "Morning Routine", "steps": ["...", "..."] },
  "MEALS":   { "title": "Breakfast Guidance", "steps": ["...", "..."] },
  "WORKOUT": { "title": "Today's Training", "steps": ["...", "..."] },
  "EVENING": { "title": "Evening Recovery", "steps": ["...", "..."] }
}
```
Loaded once at app start into a simple in-memory map via a `ContentRepository`.

DataStore (key-value, not Room):
- `hasOnboarded: Boolean`

---

## 5. Domain Logic

`WindowMatcher` — the only real "engine" logic in the app. Pure function, no side effects:

```kotlin
fun getMatchingCategories(
    now: LocalTime,
    selections: List<CategorySelection>
): List<CategorySelection> =
    selections
        .filter { it.isEnabled && now.isInWindow(it.startTime, it.endTime) }
        .sortedBy { it.startTime }

fun getNextUpcoming(
    now: LocalTime,
    selections: List<CategorySelection>
): CategorySelection? =
    selections
        .filter { it.isEnabled }
        .filter { it.startTime.isAfter(now) }
        .minByOrNull { it.startTime }
        // if none later today, wrap to earliest tomorrow
```

Streak calculation: count consecutive days (walking backward from today) where at least one `CompletionLog` exists — simplest possible definition for v1.

---

## 6. Package Structure

```
app/
 ├─ data/
 │   ├─ model/           (Category, CategorySelection, CompletionLog)
 │   ├─ local/            (Room DB, DAOs, TypeConverters)
 │   ├─ content/          (ContentRepository — loads routines.json)
 │   └─ prefs/            (DataStore wrapper for hasOnboarded)
 ├─ domain/
 │   └─ WindowMatcher.kt
 ├─ ui/
 │   ├─ onboarding/
 │   ├─ home/
 │   ├─ routine/
 │   └─ settings/
 └─ di/                   (Hilt modules)
```

---

## 7. Open Decisions Resolved for v1

- Overlapping windows → stack all matching cards on Home, ordered by start time (no priority logic).
- Content editing → bundled static JSON, not user-editable in v1.
- Per-step checkbox state → local/session only, not persisted (only the final "Done" tap is logged).
