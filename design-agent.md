# Time-Based Fitness — Design Rules (Agent Version)

Audience: coding agents implementing UI. This translates `design.md` into concrete, buildable rules. Do not invent new components, colors, or patterns beyond what's here — if a screen needs something not covered, stop and flag it rather than improvising.

Base framework: Jetpack Compose + Material3, with overridden tokens (not default Material look).

---

## 1. Color Tokens (`Color.kt`)

```kotlin
val BackgroundWarm   = Color(0xFFFAF6F0)   // base background, light mode
val SurfaceCard      = Color(0xFFFFFFFF)
val TextPrimary      = Color(0xFF1F1B16)
val TextSecondary    = Color(0xFF6B6259)

val AccentMorning    = Color(0xFFD9A441)   // soft amber
val AccentMeals      = Color(0xFFC1633B)   // terracotta
val AccentWorkout    = Color(0xFF1F5C4F)   // deep teal
val AccentEvening    = Color(0xFF5B4B8A)   // muted indigo
```
Dark mode: invert background/text (near-black background, warm off-white text), keep accents at same hue but slightly desaturated/brightened for contrast. Implement via Compose `isSystemInDarkTheme()` + a second token set — do not hardcode light-only values into components.

Accent-to-category mapping lives in one place only (`CategoryTheme.kt`), not scattered per-screen.

## 2. Spacing Scale

Use only these values (dp): `4, 8, 16, 24, 32, 48`. No arbitrary in-between values in component code.
- Card internal padding: 24
- Space between stacked Home cards: 16
- Screen horizontal margin: 24
- Section spacing (e.g. between checklist items): 16

## 3. Typography Scale

- `TitleLarge` (28sp, semibold) — current category name on Home / routine title
- `TitleMedium` (20sp, medium) — section headers
- `BodyLarge` (16sp, regular) — checklist step text
- `BodySmall` (14sp, regular) — supporting text, time windows, streak count
Max 2 font weights total (regular, semibold). No condensed/light weights.

## 4. Components

**Card (Home)**
- Rounded corners: 20dp radius
- 1dp border in the category's accent color at low opacity (~20%), not a filled/tinted background
- Elevation: flat/minimal (0-1dp) — no heavy drop shadows
- Contains: category icon (accent color), category title, short subtitle (e.g. "Window: 6:00–9:00 AM"), tap target = whole card

**Checklist item (Routine Detail)**
- Checkbox (Material3 default, tinted with category accent when checked) + body text
- 16dp vertical spacing between items
- No strikethrough animation needed beyond default Material behavior

**Primary button ("Done", "Get Started", "Save")**
- Filled, category-accent background when contextual (e.g. Done button uses current category's accent); neutral dark fill for non-contextual actions (Get Started, Save)
- Corner radius: 16dp
- Full-width on mobile, 56dp height

**Chip (Onboarding category multi-select)**
- Outlined by default, filled with category accent (20% opacity bg, full accent border) when selected
- Corner radius: full/pill shape

**Empty state (Home, nothing scheduled)**
- Centered icon (neutral, not accent-colored) + "Next up: {category} at {time}" in `TitleMedium` + `BodySmall`
- No card border/background — this is not a "card," it's a plain centered message

**Streak indicator**
- Small `BodySmall` text, top-right of Home, neutral color (TextSecondary) — not a badge, not colored, not animated

## 5. Motion

- Card/screen transitions: `fadeIn/fadeOut` + slight vertical slide (Compose `AnimatedVisibility` with `slideInVertically` ~16dp), duration 200-250ms
- Checkbox check: default Material ripple/check animation only — no custom animation
- No animation longer than 300ms anywhere in the app

## 6. Screen-Specific Rules

- **Home**: cards stacked vertically (Column), ordered by window start time ascending. Max visible without scroll should be assumed as 2-3 cards — if more overlap in practice, this is a product edge case to flag, not something to solve with denser UI.
- **Routine Detail**: single Column, checklist items, Done button pinned to bottom via `Scaffold` bottomBar or bottom-anchored Box — never requires scrolling past the button.
- **Onboarding/Settings**: one step = category selection (chips) then time windows for each selected category, shown as a simple sequential flow (can be single scrollable screen — do not over-engineer into a multi-page wizard for v1).

## 7. Theming Structure (Compose files)

```
ui/theme/
  Color.kt      (tokens above)
  Type.kt       (typography scale above)
  Shape.kt       (corner radius values)
  CategoryTheme.kt  (single source of category → accent color mapping)
  Theme.kt        (MaterialTheme wrapper, light/dark)
```
All screens consume tokens from `MaterialTheme.colorScheme` / a custom `LocalCategoryColors` composition local — never hardcode a hex value inside a screen composable.
