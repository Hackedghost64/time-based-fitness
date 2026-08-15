# Time-Based Fitness — Scaffolding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Scaffold a clean, readable, modular Android boilerplate codebase (Kotlin + Jetpack Compose + Room + DataStore + Hilt) for Time-Based Fitness v1 strictly following `master.md`, `time-based-fitness-v1-spec.md`, `agent.md`, and `design-agent.md`.

**Architecture:** Single-Activity Jetpack Compose with unidirectional data flow (Room/DataStore → Repositories → ViewModels → Composable Screens). Hilt for dependency injection, Compose Navigation for routing, and pure Kotlin domain engine for window matching logic.

**Tech Stack:** Native Kotlin, Jetpack Compose, Material3 (custom token overrides), Room DB, DataStore Preferences, Hilt, kotlinx.serialization, JUnit 4.

---

## Global Constraints

- **No AI features in v1** — pure time-based matching engine.
- **Single Activity architecture** with Jetpack Compose Navigation.
- **Min SDK 26 (Android 8.0)** — native support for `java.time.LocalTime` and `java.time.LocalDate`.
- **Color tokens & typography** strictly mapped from `design-agent.md` (`BackgroundWarm`, category accent tokens, spacing scale `4, 8, 16, 24, 32, 48`).
- **No hardcoded string/color/spacing primitives in composables** — use `ui/theme` tokens and resources.
- **Build order**: `data/model` → `data/local` → `data/content` → `data/prefs` → `domain` (TDD) → Repositories → ViewModels → UI Screens → Navigation.

---

## Proposed File & Directory Map

```
app/
 ├─ src/
 │   ├─ main/
 │   │   ├─ assets/
 │   │   │   └─ routines.json
 │   │   ├─ java/com/timebasedfitness/app/
 │   │   │   ├─ TimeBasedFitnessApp.kt
 │   │   │   ├─ MainActivity.kt
 │   │   │   ├─ data/
 │   │   │   │   ├─ model/
 │   │   │   │   │   ├─ Category.kt
 │   │   │   │   │   ├─ CategorySelection.kt
 │   │   │   │   │   └─ CompletionLog.kt
 │   │   │   │   ├─ local/
 │   │   │   │   │   ├─ Converters.kt
 │   │   │   │   │   ├─ CategorySelectionDao.kt
 │   │   │   │   │   ├─ CompletionLogDao.kt
 │   │   │   │   │   └─ AppDatabase.kt
 │   │   │   │   ├─ content/
 │   │   │   │   │   ├─ RoutineContent.kt
 │   │   │   │   │   └─ ContentRepository.kt
 │   │   │   │   ├─ prefs/
 │   │   │   │   │   └─ OnboardingPrefsRepository.kt
 │   │   │   │   └─ repository/
 │   │   │   │       ├─ CategoryRepository.kt
 │   │   │   │       └─ CompletionRepository.kt
 │   │   │   ├─ domain/
 │   │   │   │   ├─ WindowMatcher.kt
 │   │   │   │   └─ StreakCalculator.kt
 │   │   │   ├─ di/
 │   │   │   │   ├─ DatabaseModule.kt
 │   │   │   │   └─ RepositoryModule.kt
 │   │   │   └─ ui/
 │   │   │       ├─ theme/
 │   │   │       │   ├─ Color.kt
 │   │   │       │   ├─ Type.kt
 │   │   │       │   ├─ Shape.kt
 │   │   │       │   ├─ CategoryTheme.kt
 │   │   │       │   └─ Theme.kt
 │   │   │       ├─ navigation/
 │   │   │       │   └─ AppNavGraph.kt
 │   │   │       ├─ onboarding/
 │   │   │       │   ├─ OnboardingViewModel.kt
 │   │   │       │   └─ OnboardingScreen.kt
 │   │   │       ├─ home/
 │   │   │       │   ├─ HomeViewModel.kt
 │   │   │       │   └─ HomeScreen.kt
 │   │   │       ├─ routine/
 │   │   │       │   ├─ RoutineDetailViewModel.kt
 │   │   │       │   └─ RoutineDetailScreen.kt
 │   │   │       └─ settings/
 │   │   │           ├─ SettingsViewModel.kt
 │   │   │           └─ SettingsScreen.kt
 │   │   └─ AndroidManifest.xml
 │   └─ test/java/com/timebasedfitness/app/
 │       └─ domain/
 │           ├─ WindowMatcherTest.kt
 │           └─ StreakCalculatorTest.kt
 ├─ build.gradle.kts
 ├─ settings.gradle.kts
 └─ build.gradle.kts (root)
```

---

## Tasks Breakdown

### Task 1: Scaffolding Gradle & Project Structure

**Files:**
- [NEW] `settings.gradle.kts`
- [NEW] `build.gradle.kts` (root)
- [NEW] `app/build.gradle.kts`
- [NEW] `app/src/main/AndroidManifest.xml`
- [NEW] `app/src/main/assets/routines.json`
- [NEW] `app/src/main/java/com/timebasedfitness/app/TimeBasedFitnessApp.kt`

**Interfaces:**
- Configures Hilt plugin, KSP for Room, Jetpack Compose Compiler, and dependencies.
- Bundles static `routines.json` content for Morning, Meals, Workout, Evening routines.

- [ ] **Step 1: Create `settings.gradle.kts` and root `build.gradle.kts`**
- [ ] **Step 2: Create `app/build.gradle.kts` with Room, Hilt, DataStore, Compose dependencies**
- [ ] **Step 3: Create `AndroidManifest.xml` and `TimeBasedFitnessApp.kt` annotated with `@HiltAndroidApp`**
- [ ] **Step 4: Create `app/src/main/assets/routines.json` with static checklist content**

```json
{
  "MORNING": { "title": "Morning Routine", "steps": ["Hydrate with 500ml water", "Light 5-min mobility stretch", "10 deep belly breaths", "Healthy protein-rich breakfast"] },
  "MEALS": { "title": "Breakfast & Fuel", "steps": ["Balanced meal with whole foods", "Stay hydrated", "Avoid heavy processed sugars"] },
  "WORKOUT": { "title": "Today's Training", "steps": ["5-min dynamic warm-up", "Core exercise set (3x12 reps)", "Bodyweight or resistance main movement", "Cool-down static stretch"] },
  "EVENING": { "title": "Evening Recovery", "steps": ["Dim screens 1hr before bed", "5-min gentle hamstring & hip stretch", "Review today's wins", "Sleep environment temperature check"] }
}
```

---

### Task 2: UI Design System & Theme (`ui/theme`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/theme/Color.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/theme/Type.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/theme/Shape.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/theme/CategoryTheme.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/theme/Theme.kt`

**Interfaces:**
- `CategoryTheme`: maps `Category` enum to `AccentMorning`, `AccentMeals`, `AccentWorkout`, `AccentEvening`.
- `Theme.kt`: provides light and dark themes using `design-agent.md` color tokens.

- [ ] **Step 1: Create `Color.kt` with exact tokens (`BackgroundWarm = 0xFFFAF6F0`, category accents)**
- [ ] **Step 2: Create `CategoryTheme.kt` for single-source category color mapping**
- [ ] **Step 3: Create `Type.kt` & `Shape.kt` defining typography and 16dp/20dp rounded corners**
- [ ] **Step 4: Create `Theme.kt` wrapping MaterialTheme**

---

### Task 3: Data Layer — Models, Room DB & Converters (`data/model`, `data/local`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/model/Category.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/model/CategorySelection.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/model/CompletionLog.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/local/Converters.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/local/CategorySelectionDao.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/local/CompletionLogDao.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/local/AppDatabase.kt`

**Interfaces:**
```kotlin
enum class Category { MORNING, MEALS, WORKOUT, EVENING }

@Entity
data class CategorySelection(
    @PrimaryKey val category: Category,
    val isEnabled: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime
)

@Entity(primaryKeys = ["date", "category"])
data class CompletionLog(
    val date: LocalDate,
    val category: Category,
    val completedAt: Instant
)
```

- [ ] **Step 1: Write entities and enums (`Category`, `CategorySelection`, `CompletionLog`)**
- [ ] **Step 2: Write Room `TypeConverters` converting `LocalTime` (minutes Int), `LocalDate` (epoch day Int), and `Instant` (epoch millis Long)**
- [ ] **Step 3: Write DAOs (`CategorySelectionDao`, `CompletionLogDao`) with Flow return types for reactive UI updates**
- [ ] **Step 4: Create `AppDatabase.kt` Room Database class**

---

### Task 4: Preferences & Static Content Data Layer (`data/prefs`, `data/content`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/prefs/OnboardingPrefsRepository.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/content/RoutineContent.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/content/ContentRepository.kt`

**Interfaces:**
- `OnboardingPrefsRepository`: exposes `hasOnboarded: Flow<Boolean>` and `setOnboarded(Boolean)`.
- `ContentRepository`: exposes `getRoutine(Category): RoutineContent?` by loading `routines.json`.

- [ ] **Step 1: Implement `OnboardingPrefsRepository` backed by DataStore Preferences**
- [ ] **Step 2: Implement `ContentRepository` parsing `assets/routines.json` via `kotlinx.serialization` or `Gson`**

---

### Task 5: Domain Engine — `WindowMatcher` & Unit Tests (`domain`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/domain/WindowMatcher.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/domain/StreakCalculator.kt`
- [NEW] `app/src/test/java/com/timebasedfitness/app/domain/WindowMatcherTest.kt`
- [NEW] `app/src/test/java/com/timebasedfitness/app/domain/StreakCalculatorTest.kt`

**Interfaces:**
```kotlin
object WindowMatcher {
    fun getMatchingCategories(now: LocalTime, selections: List<CategorySelection>): List<CategorySelection>
    fun getNextUpcoming(now: LocalTime, selections: List<CategorySelection>): CategorySelection?
    fun isInWindow(now: LocalTime, startTime: LocalTime, endTime: LocalTime): Boolean
}

object StreakCalculator {
    fun calculateStreak(logs: List<CompletionLog>, today: LocalDate = LocalDate.now()): Int
}
```

- [ ] **Step 1: Write failing unit test `WindowMatcherTest.kt` testing normal windows, midnight-crossing windows (e.g. 22:00 to 02:00), boundary conditions, and sorting**
- [ ] **Step 2: Implement `WindowMatcher.kt` handling midnight wraparound (`startTime > endTime`)**
- [ ] **Step 3: Run unit tests to verify `WindowMatcherTest` passes**
- [ ] **Step 4: Write failing unit test `StreakCalculatorTest.kt` testing consecutive days backward calculation**
- [ ] **Step 5: Implement `StreakCalculator.kt` and verify test passes**

---

### Task 6: Repositories & Dependency Injection (`data/repository`, `di`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/repository/CategoryRepository.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/data/repository/CompletionRepository.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/di/DatabaseModule.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/di/RepositoryModule.kt`

**Interfaces:**
- `CategoryRepository`: handles reading/writing `CategorySelection` list in Room with default values pre-populated.
- `CompletionRepository`: handles logging completion and providing reactive streak calculations.
- `DatabaseModule` & `RepositoryModule`: Hilt `@Module` and `@InstallIn(SingletonComponent::class)`.

- [ ] **Step 1: Create repositories wrapping DAOs and domain calculators**
- [ ] **Step 2: Implement Hilt modules for Room DB, DAOs, DataStore, and Repositories**

---

### Task 7: Screen ViewModels (`ui/*`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/onboarding/OnboardingViewModel.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/home/HomeViewModel.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/routine/RoutineDetailViewModel.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/settings/SettingsViewModel.kt`

**Interfaces:**
- UI State defined using Kotlin sealed interfaces/classes (e.g. `HomeUiState.Loading`, `HomeUiState.Content`, `HomeUiState.Empty`).
- ViewModels expose `StateFlow<UiState>`.

- [ ] **Step 1: Build `OnboardingViewModel` (manages category selection chips & time pickers state)**
- [ ] **Step 2: Build `HomeViewModel` (evaluates `WindowMatcher` with current local time and current streak)**
- [ ] **Step 3: Build `RoutineDetailViewModel` (loads routine content, logs completion)**
- [ ] **Step 4: Build `SettingsViewModel` (pre-filled category selection editor)**

---

### Task 8: UI Screens, Navigation & App Entry (`ui/*`, `MainActivity`)

**Files:**
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/onboarding/OnboardingScreen.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/home/HomeScreen.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/routine/RoutineDetailScreen.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/settings/SettingsScreen.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/ui/navigation/AppNavGraph.kt`
- [NEW] `app/src/main/java/com/timebasedfitness/app/MainActivity.kt`

**UI Features according to `design-agent.md`:**
- **OnboardingScreen**: Outlined chips, time pickers, primary action button.
- **HomeScreen**: Stacked cards (20dp rounded corners, accent border), top-right streak count, empty "Next up" state.
- **RoutineDetailScreen**: Static checklist with category-accented checkboxes, pinned full-width "Done" button at bottom.
- **SettingsScreen**: Edit selections and windows, returning to Home.

- [ ] **Step 1: Implement `OnboardingScreen.kt`**
- [ ] **Step 2: Implement `HomeScreen.kt`**
- [ ] **Step 3: Implement `RoutineDetailScreen.kt`**
- [ ] **Step 4: Implement `SettingsScreen.kt`**
- [ ] **Step 5: Wire up `AppNavGraph.kt` handling start destination conditional on `hasOnboarded` DataStore preference**
- [ ] **Step 6: Update `MainActivity.kt` with `@AndroidEntryPoint` and set Compose content**

---

## Verification Plan

### Automated Verification
- **Domain Engine Unit Tests**: Run `./gradlew test` (or `./gradlew :app:testDebugUnitTest`) to verify `WindowMatcherTest` and `StreakCalculatorTest` pass with 100% test success.
- **Build Verification**: Run `./gradlew assembleDebug` to ensure all generated code, Room DAOs, KSP/Hilt annotations compile cleanly without warnings or errors.

### Manual Verification Flow
1. **Cold Start (No Data)**: App launches → DataStore `hasOnboarded` is false → Onboarding screen shown.
2. **Onboarding**: Select categories (e.g. Morning: 06:00-09:00, Workout: 17:00-19:00) → Tap "Get Started" → Saved to Room DB & DataStore → Navigates to Home.
3. **Home Screen**:
   - If current time is within window → card is displayed in category accent border.
   - If outside window → calm empty state showing "Next up: [Category] at [Time]".
4. **Routine Detail**: Tap card → checklist displayed → toggle checkboxes → tap "Done" → CompletionLog written to Room → returns to Home → streak counter increments.
5. **Settings**: Tap settings icon on Home → edit time windows → Save → returns to Home with updated schedule.
