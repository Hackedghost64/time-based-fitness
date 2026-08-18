# Time-Based Fitness v2 Roadmap

## Executive Summary

Following the technical architecture audit (score: 8.2/10) and frontend analysis, this document outlines the v2 improvement plan. The focus is on **production hardening first**, followed by **targeted feature expansion** that maintains the app's core philosophy of minimalism and time-contextual UX.

---

## Phase 1: Production Hardening (P0/P1 Issues)

### 1.1 Canonical TimeWindow Abstraction ✅ COMPLETED

**Problem**: Different subsystems interpreted time windows independently, causing inconsistencies for overnight windows (e.g., 22:00–02:00).

**Solution**: Introduced `TimeWindow` data class as the single source of truth for all time-window semantics.

**Files Changed**:
- `/domain/TimeWindow.kt` — New canonical abstraction with 10+ methods
- `/domain/WindowMatcher.kt` — Refactored to delegate to `TimeWindow`
- `/notifications/NotificationScheduler.kt` — Refactored `calculateNextNudgeMillis()` to use `TimeWindow`
- `/domain/TimeWindowTest.kt` — 25+ comprehensive test cases

**Benefits**:
- Consistent behavior across window matching, notifications, widgets, and countdowns
- Correct handling of midnight-crossing windows throughout the app
- Easier to test temporal edge cases in isolation

### 1.2 Completion Uniqueness Constraint

**Current State**: Already implemented via composite primary key `(date, category)` in `CompletionLog` entity.

```kotlin
@Entity(
    tableName = "completion_logs",
    primaryKeys = ["date", "category"]  // ✅ Prevents duplicates
)
data class CompletionLog(...)
```

**Action**: No changes needed — already production-ready.

### 1.3 runBlocking Bridge in Scheduler

**Current State**: `NotificationScheduler.scheduleNextReminder()` uses `runBlocking` to query completion state from the suspend repository.

**Impact**: Works correctly but introduces blocking into scheduling code.

**v2 Recommendation**: Refactor to coroutine-aware design when alarm scheduling can be fully async. Low priority since current implementation is stable and tested.

### 1.4 Centralized Clock/TimeProvider

**Current State**: Distributed calls to `LocalDate.now()`, `LocalDateTime.now()`, `ZoneId.systemDefault()`.

**v2 Implementation Plan**:

```kotlin
interface TimeProvider {
    fun now(): LocalDateTime
    fun today(): LocalDate
    fun zoneId(): ZoneId
}

class SystemTimeProvider : TimeProvider {
    override fun now() = LocalDateTime.now()
    override fun today() = LocalDate.now()
    override fun zoneId() = ZoneId.systemDefault()
}

class TestTimeProvider(private val fixedNow: LocalDateTime) : TimeProvider {
    override fun now() = fixedNow
    override fun today() = fixedNow.toLocalDate()
    override fun zoneId() = ZoneId.of("UTC")
}
```

**Benefits**:
- Deterministic testing for overnight windows, timezone changes, DST transitions
- Single point of control for time semantics
- Easier to simulate edge cases (travel, clock changes)

---

## Phase 2: UI/UX Enhancements (V2 Features)

### 2.1 Haptic Feedback for Timer Completion

**File**: `/ui/routine/RoutineDetailViewModel.kt`

Add haptic feedback when timer completes:

```kotlin
@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    // ... existing deps
    private val hapticFeedback: HapticFeedback
) : ViewModel() {

    private fun onTimerComplete() {
        hapticFeedback.vibrate(TimerCompletePattern)
        // ... existing completion logic
    }
}

object HapticPatterns {
    val TimerCompletePattern = VibrationEffect.createWaveform(
        longArrayOf(0, 100, 50, 100), // pulse-pause-pulse
        -1
    )
}
```

**User Impact**: Tactile confirmation without looking at screen — crucial for workout scenarios.

### 2.2 Micro-animations for Streak Milestones

**File**: `/ui/home/HomeScreen.kt`

Enhance streak tile with celebration animation at milestones (7, 30, 100 days):

```kotlin
@Composable
private fun StreakTile(streak: Int, bestStreak: Int, completedDates: Set<LocalDate>) {
    val isMilestone = streak in listOf(7, 30, 60, 90, 100, 365)
    val scale = remember { Animatable(1f) }

    LaunchedEffect(streak) {
        if (isMilestone) {
            scale.animateTo(1.2f, tween(150))
            scale.animateTo(1f, tween(150))
        }
    }

    Surface(modifier = Modifier.scale(scale.value)) {
        // ... existing tile content
    }
}
```

### 2.3 Progress Charts (Most Requested v2 Feature)

**New Screen**: `/ui/progress/ProgressScreen.kt`

Minimalist charts showing:
- **Volume over time**: Steps/exercises completed per week
- **Consistency heat map**: GitHub-style contribution graph
- **Streak history**: Longest streaks, recent trends

```kotlin
@Composable
fun ProgressChart(viewModel: ProgressViewModel) {
    val weeklyData by viewModel.weeklyCompletionFlow.collectAsState()
    
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        // Simple bar chart using Compose Canvas
        weeklyData.forEachIndexed { index, count ->
            drawRect(
                color = MaterialTheme.colorScheme.primary,
                topLeft = Offset(index * barWidth, size.height - count.normalized),
                size = Size(barWidth - 4, count.normalized)
            )
        }
    }
}
```

**Philosophy**: Charts should be **quiet and contextual** — not a dashboard overhaul. Accessible via Settings → Progress, not home screen.

### 2.4 Dynamic Color (Material You)

**File**: `/ui/theme/Theme.kt`

Enable Android 12+ dynamic theming:

```kotlin
@Composable
fun TimeBasedFitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // NEW
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            DynamicColor.retrieveDynamicColors(context, darkTheme)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

**User Impact**: App adapts to user's wallpaper preferences — feels more integrated.

### 2.5 Widget Improvements

**File**: `/widget/NowRoutineWidget.kt`

Add interactive elements to widget:
- **Quick-complete button**: Mark routine done without opening app
- **Snooze nudge**: Postpone next reminder by 15 min
- **Expanded layout**: Show 2-3 step preview in expanded widget state

```kotlin
// Glance action buttons
ActionButton(
    onClick = ActionCallback.CompleteRoutine::class.java,
    text = "✓ Complete",
    icon = IconRes(R.drawable.ic_check)
)
```

---

## Phase 3: Platform Expansion

### 3.1 Wear OS Companion Tile

**Scope**: Minimal tile showing:
- Current/next routine name
- Timer countdown (if active)
- Quick-complete button

**Architecture**: Share `TimeWindow`, `RoutineRepository` via shared module.

### 3.2 Google Health Connect Integration

**Optional sync** (user-enabled):
- Write workout completions to Health Connect
- Read sleep data to suggest optimal evening wind-down time

**Privacy**: All sync is opt-in, local-first remains default.

### 3.3 Collaborative Plans (JSON Sharing)

**Enhancement**: Add share intent handler for `.tbfitness` JSON files.

```kotlin
// In MainActivity
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (intent?.type == "application/json") {
        handleImportIntent(intent.data)
    }
}
```

**Use Case**: Trainer shares plan via WhatsApp/email → 1-tap import.

---

## Phase 4: AI Enhancements (Optional Layer)

### 4.1 Adaptive Scheduling Suggestions

**Current**: AI generates one-time plan via JSON export/import.

**v2**: Periodic suggestions based on completion patterns:

> "You've missed Workout routines on Fridays 3 weeks in a row. Want to shift your workout window to Saturday morning?"

**Implementation**: Analyze `CompletionLog` history, surface as non-intrusive notification.

### 4.2 Smart Goal Adjustment

If user consistently completes routines early:

> "You're crushing your 10-min morning routine in 7 minutes. Want to level up?"

---

## Testing Strategy for v2

### New Test Coverage Required

| Area | Test Cases |
|------|-----------|
| **TimeWindow** | ✅ 25+ tests covering normal/overnight windows, edge cases |
| **TimeProvider** | Deterministic time simulation, TZ changes, DST transitions |
| **Haptics** | Verify vibration patterns on physical device |
| **Charts** | Empty state, max values, long streak rendering |
| **Widget Actions** | Quick-complete, snooze, expanded state updates |
| **Dynamic Color** | Fallback on pre-Android 12, correct color extraction |

### Existing Tests to Update

- `NotificationSchedulerTest.kt` — Use `TimeWindow` assertions instead of ad-hoc math
- `WindowMatcherTest.kt` — Ensure consistency with `TimeWindowTest`

---

## Architecture After v2

```
app/
├── domain/
│   ├── TimeWindow.kt          ← NEW: Canonical time semantics
│   ├── TimeProvider.kt        ← NEW: Abstracted clock
│   ├── WindowMatcher.kt       ← Uses TimeWindow
│   └── StreakCalculator.kt
├── data/
│   ├── repository/
│   │   ├── RoutineRepository.kt
│   │   ├── CompletionRepository.kt
│   │   └── ProgressRepository.kt  ← NEW: Chart data
│   └── prefs/
├── ui/
│   ├── home/
│   ├── routine/
│   ├── progress/              ← NEW: Charts screen
│   └── theme/
├── widget/
│   └── NowRoutineWidget.kt    ← Enhanced actions
└── di/
```

---

## Success Metrics for v2

| Metric | Target |
|--------|--------|
| **Crash-free sessions** | > 99.5% |
| **Overnight window bugs** | 0 reported |
| **Test coverage** | > 80% domain layer |
| **Widget engagement** | 30% of users interact weekly |
| **Progress screen adoption** | 20% of users view monthly |
| **Play Store rating** | Maintain 4.5+ stars |

---

## Timeline Estimate

| Phase | Duration | Priority |
|-------|----------|----------|
| **Phase 1: Hardening** | 2 weeks | P0 |
| **Phase 2: UI Enhancements** | 3 weeks | P1 |
| **Phase 3: Platform Expansion** | 4 weeks | P2 |
| **Phase 4: AI Enhancements** | 2 weeks | P3 |

**Total**: 11 weeks for full v2 scope. Core hardening (Phase 1) can ship independently.

---

## Final Notes

The v1 architecture is **strong** (8.2/10). The P0 time-window inconsistency has been resolved with the `TimeWindow` abstraction. The remaining work is **incremental hardening and thoughtful expansion** — not rewrites.

**Key Principle**: Every v2 feature must pass the "Open → Follow → Done" test. If it adds cognitive load or navigation steps, it doesn't belong in this app.
