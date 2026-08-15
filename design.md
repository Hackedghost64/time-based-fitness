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
