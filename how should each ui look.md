---
name: Quiet Ritual
colors:
  surface: '#fdf8f8'
  surface-dim: '#ddd9d8'
  surface-bright: '#fdf8f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f3f2'
  surface-container: '#f1edec'
  surface-container-high: '#ebe7e7'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1c1b1b'
  on-surface-variant: '#444748'
  inverse-surface: '#313030'
  inverse-on-surface: '#f4f0ef'
  outline: '#747878'
  outline-variant: '#c4c7c7'
  surface-tint: '#5f5e5e'
  primary: '#0a0a0a'
  on-primary: '#ffffff'
  primary-container: '#212121'
  on-primary-container: '#898888'
  inverse-primary: '#c8c6c5'
  secondary: '#5e5e5c'
  on-secondary: '#ffffff'
  secondary-container: '#e1dfdc'
  on-secondary-container: '#636360'
  tertiary: '#0b0a09'
  on-tertiary: '#ffffff'
  tertiary-container: '#222120'
  on-tertiary-container: '#8b8886'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5e2e1'
  primary-fixed-dim: '#c8c6c5'
  on-primary-fixed: '#1b1c1c'
  on-primary-fixed-variant: '#474746'
  secondary-fixed: '#e4e2df'
  secondary-fixed-dim: '#c8c6c4'
  on-secondary-fixed: '#1b1c1a'
  on-secondary-fixed-variant: '#474745'
  tertiary-fixed: '#e6e2e0'
  tertiary-fixed-dim: '#c9c6c4'
  on-tertiary-fixed: '#1c1b1a'
  on-tertiary-fixed-variant: '#484645'
  background: '#fdf8f8'
  on-background: '#1c1b1b'
  surface-variant: '#e5e2e1'
  morning-amber: '#D4A373'
  meals-terracotta: '#B05B3B'
  workout-teal: '#2D6A4F'
  evening-indigo: '#4A4E69'
  background-warm: '#FAF9F6'
typography:
  headline-lg:
    fontFamily: Manrope
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  margin-page: 2rem
  stack-gap: 1.5rem
  card-padding: 2rem
  list-item-height: 4rem
---

# Time-Based Fitness — Design Direction (Designer Version)

Audience: a design AI / human designer producing visual direction, mockups, or UI exploration. No code or implementation detail here — see `design-agent.md` for that.

---

## Product Essence

One card. Right time. No thinking. The entire product experience is: open the app, see exactly one thing you're meant to do right now, do it, close the app. Everything in the design should serve that — nothing should compete with the current card for attention.

## Design Principles

1. **Single-focus, not dashboard.** Never show the user their whole day at once. One "now" card dominates the screen. History, settings, streaks — all secondary, tucked away.
2. **Calm over energetic.** This is not a gamified, notification-badge, confetti-on-completion app. It should feel like a quiet assistant, not a coach yelling at you. Think "a good habit," not "a workout hype app."
3. **Generous whitespace.** The app should never feel dense. If a screen feels like it needs a scroll to see everything, it's probably too busy for this product.
4. **Time-of-day as a visual cue, not a gimmick.** Each category (Morning, Meals, Workout, Evening) can have a distinct, subtle accent — but restrained, not a garish theme-switcher.
5. **Trustworthy plainness.** Typography and layout should read as clear and considered, not trendy. This app should feel like it'll still look good in five years.

## Color Direction

- Base palette: warm neutral (off-white / soft warm gray background, near-black text) — avoid clinical pure white or pure black.
- One quiet accent color per category, used sparingly (a card border, an icon, a button) — not full-screen tinting:
  - Morning → soft amber/gold
  - Meals → warm terracotta
  - Workout → deep teal or forest green
  - Evening → muted indigo/plum
- No bright saturated "app icon" colors dominating the UI. Accents should feel like a highlight, not a background.

## Typography

- One clean, humanist sans-serif. Legible at a glance — the user should be able to read the current card in under 2 seconds.
- Strong size hierarchy: the category name / current action is the biggest thing on screen. Supporting text (steps, times) recedes.
- Avoid more than 2 weights (regular + semibold/medium) — no need for a large type system in an app this simple.

## Layout & Spacing Feel

- Home screen: one large card centered, generous margin around it. If multiple categories overlap, stack cards with clear separation — never let them feel like a list/table.
- Routine detail: a simple vertical checklist, plenty of line height, one "Done" action clearly anchored at the bottom.
- Onboarding: one decision per screen/step feels better than a single dense form — but keep it to as few taps as possible.
- Empty state (nothing scheduled right now) should feel intentional and calm, not like an error or blank slate — e.g. "Next up: Workout at 6:00 PM" with quiet, friendly tone.

## Iconography

- Simple, single-weight line icons — one per category (sun for Morning, fork/plate for Meals, dumbbell for Workout, moon for Evening). No cartoonish or 3D icon sets.

## Motion

- Minimal. A gentle fade/slide when a card changes or a routine is marked done. No bouncy, playful, or attention-grabbing animation — motion should reinforce calm, not excitement.

## Voice & Microcopy

- Short, plain, encouraging without being peppy. "Today's Workout" not "Let's crush today's workout! 💪"
- Empty/next-up states should feel like a helpful nudge, not a nag.

## What to Avoid

- Dashboards, calendars, or grid/list views of "everything at once"
- Streaks/badges presented loudly (a small quiet number is enough — no fire emojis, no celebratory overlays)
- Dense settings screens — settings should look and feel like onboarding, not a config panel
- Any visual implying the app is "watching" or "tracking" the user aggressively
