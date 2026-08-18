# Time-Based Fitness

A minimal Android fitness app built around **when you do things**, not a timeline or dashboard.

## The Idea

Instead of asking the user to plan their fitness routine, the app organises guidance around the natural time periods of their day.

During onboarding, the user tells the app when they normally:

- Wake up
- Eat meals
- Work out
- Sleep / wind down

The app then surfaces the right routine card the moment they open it.

### Example

| Time | What you see |
|---|---|
| Morning | Morning routine / breakfast guidance |
| Lunch | Meal timing guidance |
| Workout | Today's session — exercises, sets/reps, form cues, countdown timers |
| Evening | Recovery / wind-down routine |

> **Open → Follow → Done.**

---

## Core Philosophy

- Minimal interface — one card, one action
- Almost zero planning required from the user
- Time-period-first UX
- Clear, actionable instructions
- Consistency over complexity
- AI is optional, not required for core use

---

## Features (v1.5 — current)

### Scheduling & Routing
- **Time-window engine** — matches the current clock time against user-configured windows (Morning, Meals, Workout, Evening)
- **Overnight window support** — windows that span midnight work correctly
- **Weekday splits** — routines can define different steps per day of the week; case-insensitive key matching handles legacy import formats
- **Zone-aware day lookup** — day-of-week is derived from the device's timezone, not the JVM default

### Home Screen
- Single focused card showing the active or next-up routine
- **Streak tile** — animated display of current streak; graduates to a bold gradient banner at 7+ days
- **7-day completion dots** — quiet visual history row beneath the card
- Active-window badge, next-window countdown, and multi-active overflow label

### Routine Detail
- Step-by-step walkthrough with per-step countdown timers
- **Timer soft gate** — tapping "Done" before the recommended time shows a soft prompt rather than blocking
- Timer state persists across app restarts (DataStore-backed)
- Auto-advance to next timed step on completion

### Notifications
- **NudgePolicy** — configurable interval (default 10 min) and per-window cap (default 6 nudges)
- Completion-aware: stops firing once a routine is marked done
- Exact alarms on API < 31; falls back gracefully to inexact on API 31+ when exact permission is denied
- Reschedule receiver re-arms alarms on boot, TZ change, and app upgrade

### Home-Screen Widget (Glance)
- **Active** state: category colour, title, step progress, minutes remaining
- **Next** state: upcoming routine name, start time, streak chip
- **Idle** state: prompt to open the app
- Pulls live data via Hilt entry points; no polling tick required

### AI & Plan Exchange
- JSON plan import/export with schema validation, AI-repair cleaner, and 1-tap undo
- `AiPromptBuilder` generates structured LLM prompts for personalised plan creation
- Preview dialog before applying an imported plan
- All AI features are optional — core experience works without them

### Accessibility
- TalkBack live-region announcements on timer countdown
- Semantic content descriptions on all interactive elements
- Minimum 48 dp touch targets throughout

### Settings
- Nudge interval picker (5 / 10 / 15 / 30 min)
- Max nudges per window (0–48)
- Window schedule editor (start/end time per category)
- Plan transfer screen (import / export JSON)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| DI | Hilt |
| Database | Room (+ type converters for `LocalDate`, `LocalTime`, `Instant`) |
| Preferences | DataStore Preferences |
| Widget | Glance AppWidget |
| Serialisation | `kotlinx.serialization` |
| Build | Gradle (KTS), AGP 8.x, KSP |
| Min SDK | 26 (Android 8) |
| Target SDK | 36 |

---

## Architecture

```
app/
├── data/
│   ├── content/        # Built-in routine content, AI prompt builder, plan JSON codec
│   ├── local/          # Room DAOs and AppDatabase
│   ├── model/          # Domain models (RoutineStep, RoutineEntity, CompletionLog, …)
│   ├── prefs/          # DataStore — onboarding state, nudge settings, timer persistence
│   └── repository/     # RoutineRepository, CompletionRepository, CategoryRepository
├── di/                 # Hilt modules (DatabaseModule, RepositoryModule)
├── domain/
│   ├── WindowMatcher.kt      # Time-window matching logic (pure, testable)
│   └── StreakCalculator.kt   # Current + best streak from completion logs
├── notifications/
│   ├── NotificationScheduler.kt   # NudgePolicy + alarm scheduling
│   ├── RoutineReminderReceiver.kt # BroadcastReceiver — fires notification, records nudge
│   ├── RescheduleReceiver.kt      # Re-arms alarms on boot / TZ change
│   └── TimerNotificationHelper.kt
├── ui/
│   ├── home/           # HomeScreen + HomeViewModel
│   ├── onboarding/     # OnboardingScreen + OnboardingViewModel
│   ├── routine/        # RoutineDetailScreen + RoutineDetailViewModel (timers, soft gate)
│   ├── settings/       # SettingsScreen, AiPlanScreen, PlanTransferScreen
│   ├── navigation/     # AppNavGraph
│   └── theme/          # Color, Type, Shape, Motion, CategoryTheme, CategoryChips
└── widget/
    ├── WidgetSnapshot.kt    # Pure state model + SharedPrefs persistence
    └── NowRoutineWidget.kt  # Glance composable (Active / Next / Idle)
```

---

## Build

### Debug

```bash
./gradlew assembleDebug
```

### Release AAB (signed)

```bash
export KEYSTORE_PATH=/path/to/your.keystore
export KEYSTORE_PASSWORD=...
export KEY_ALIAS=...
export KEY_PASSWORD=...
./gradlew bundleRelease
```

Or use the convenience script (credentials pre-configured):

```bash
bash build-release.sh
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### Run unit tests

```bash
./gradlew testDebugUnitTest
```

**46 unit tests** covering:
- Window matching (active / next / overnight / disabled)
- Streak calculation (current + best)
- Weekday split lookup (case-insensitive, zone-aware)
- Notification scheduler nudge logic (interval, cap, completion gate)
- Widget snapshot state machine
- Step timer gating
- Plan JSON codec and AI repair cleaner

---

## Status

**v1.5** — feature-complete for Play Store submission.

### Shipped in v1.x series

- [x] Onboarding — configure time windows
- [x] Home screen — time-period-first single card
- [x] Routine detail — step-by-step with countdown timers
- [x] Weekday splits — different steps per day
- [x] 7-day completion history
- [x] Streak tracking + streak tile
- [x] Push notifications with nudge cadence and per-window cap
- [x] Home-screen widget (Active / Next / Idle)
- [x] JSON plan import/export with AI repair and undo
- [x] AI prompt builder for external plan generation
- [x] Accessibility (TalkBack, semantic labels, 48 dp targets)
- [x] Settings — nudge interval, max nudges, window schedule editor

### Possible v2 additions

- [ ] Progress charts (volume, frequency over time)
- [ ] Apple Health / Google Health Connect integration
- [ ] Collaborative plans (share a JSON plan via link)
- [ ] Wear OS companion tile
- [ ] AI-driven adaptive scheduling (optional layer)

---

## Product Question

**Can a fitness app become dramatically simpler by appearing at the right moment instead of asking the user to plan everything themselves?**
