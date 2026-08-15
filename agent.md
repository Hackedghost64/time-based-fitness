# Time-Based Fitness — Agent Instructions

Audience: coding agents building this app. Read `master.md` first for how this doc relates to the others, then `time-based-fitness-v1-spec.md` for scope/data model, then `design-agent.md` for UI rules. This doc covers architecture, process, and what to do when something isn't covered.

---

## Architecture

- Pattern: simple MVVM. One `ViewModel` per screen (Onboarding, Home, RoutineDetail, Settings), exposing UI state via `StateFlow`.
- Data flow is one-directional: Room/DataStore → Repository → ViewModel → Composable UI. UI never talks to Room/DataStore directly.
- Repositories: `CategorySelectionRepository` (Room), `CompletionLogRepository` (Room), `ContentRepository` (bundled JSON asset), `OnboardingPrefsRepository` (DataStore).
- DI: Hilt for all repositories and ViewModels. No manual singleton wiring.
- Navigation: Compose Navigation, single-activity. Routes: `onboarding`, `home`, `routine/{category}`, `settings`.

## Build Order

Build and verify in this order — don't jump ahead to UI before the layer below it compiles and (where applicable) has a basic test:

1. `data/model` — Category enum, CategorySelection, CompletionLog data classes
2. `data/local` — Room entities, DAOs, TypeConverters (LocalTime/LocalDate ↔ stored primitives), Database class
3. `data/content` — ContentRepository loading `routines.json` from assets
4. `data/prefs` — DataStore wrapper for `hasOnboarded`
5. `domain/WindowMatcher.kt` — pure function, no Android dependencies. Write this with unit tests before touching UI.
6. Repositories wrapping the above
7. ViewModels (Onboarding → Home → RoutineDetail → Settings, in that order)
8. UI screens per `design-agent.md`, wired to ViewModels
9. Navigation graph connecting all screens
10. Manual pass: cold start (no data) → onboarding → home → routine → done → home again → settings → back to home

## Code Conventions

- Package structure exactly as defined in the spec (`data/model`, `data/local`, `data/content`, `data/prefs`, `domain`, `ui/{screen}`, `di`) — don't reorganize without flagging why.
- Kotlin idiomatic style: data classes for models, sealed classes/interfaces for UI state (e.g. `sealed interface HomeUiState { object Loading; data class Content(...); object Empty }`).
- No hardcoded strings in composables for anything user-facing beyond truly static labels — but a full i18n/strings.xml setup is fine and expected (standard Android practice), not overkill for this app.
- No hardcoded colors/spacing/type sizes in composables — always reference `design-agent.md` tokens via the theme files.

## Edge Cases to Handle Explicitly

- Zero categories enabled after onboarding (user deselects everything in Settings) → Home should show a calm "nothing set up — go to Settings" state, not crash or show a blank screen.
- A time window that crosses midnight (e.g. Evening 10:00 PM–2:00 AM) → `WindowMatcher` must handle wraparound, not just assume start < end.
- App opened for the very first time with system clock/timezone edge cases → use device local time consistently, no timezone conversion needed since this is single-device local scheduling.
- Two categories with identical start times → stable sort, order doesn't matter functionally, just don't crash/flicker.

## Testing Expectations

- `WindowMatcher` gets real unit tests: standard window, midnight-crossing window, no match, multiple matches, boundary conditions (exactly at start/end time).
- Other layers: basic smoke coverage is enough for v1 — this is a small solo-shipped app, not enterprise software. Don't over-invest in test infrastructure beyond the domain logic that actually has real logic in it.

## Non-Negotiables (from spec — do not silently add scope)

- No AI features in v1.
- No exercise library / content database — routines are static, bundled JSON.
- No push notifications/reminders.
- No per-step persistence — only the final "Done" tap is logged.
- No nutrition macro tracking.

## When Something Isn't Covered

If a screen, interaction, or edge case comes up that isn't specified in the spec, `design-agent.md`, or this doc — stop and ask rather than assuming or inventing a pattern. This is a deliberately small v1; scope creep during implementation is the main risk to actually shipping it.
