# Time-Based Fitness — Master Index

One-line summary: a minimal Android app (Kotlin + Jetpack Compose) that shows the user the right routine card based on the time of day, instead of asking them to plan anything. No AI in v1. Goal: ship a small, genuinely useful v1 to the Play Store, then iterate.

---

## Document Map

| File | Audience | Purpose |
|---|---|---|
| `time-based-fitness-v1-spec.md` | Everyone (source of truth on scope) | What v1 does, screens, state flow, data model, package structure |
| `design.md` | Design AI / human designer | Visual direction, mood, color/type feel, screen-by-screen aesthetic intent — no implementation detail |
| `design-agent.md` | Coding agents (UI work) | Concrete design tokens, component specs, spacing/type scale — the buildable translation of `design.md` |
| `agent.md` | Coding agents (all work) | Architecture, build order, code conventions, edge cases, non-negotiables, process rules |
| `master.md` (this file) | Everyone | Orientation — what to read, in what order, and which doc wins if two disagree |

## Reading Order

1. `master.md` — this file, for orientation
2. `time-based-fitness-v1-spec.md` — understand what's being built before anything else
3. `design.md` — (design AI only) visual direction, to produce mockups/exploration
4. `design-agent.md` + `agent.md` — (coding agent) implementation rules, read together before writing any code

## Source of Truth Hierarchy

If two documents ever conflict:
1. **Scope conflicts** (is X in v1?) → `time-based-fitness-v1-spec.md` wins.
2. **Visual/UI conflicts** (color, spacing, component look) → `design-agent.md` wins over `design.md` (the agent doc is the buildable, already-resolved version of the designer doc).
3. **Process/architecture conflicts** → `agent.md` wins.

If a real conflict is found between docs, flag it rather than silently picking one — it likely means the docs need a fix, not just the code.

## Current Status

Phase: spec + design + agent docs complete. Next: hand `time-based-fitness-v1-spec.md` + `agent.md` + `design-agent.md` to a coding agent for boilerplate scaffolding, following the build order in `agent.md` section "Build Order."

## Non-Negotiables (repeated here for quick reference)

- No AI in v1.
- Native Kotlin + Jetpack Compose.
- Minimal UI: one card, one action, no dashboard.
- Ship a small working v1 to the Play Store first; expand via updates after.
