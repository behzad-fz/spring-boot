# Backlog Planner — Design

Date: 2026-08-16
Status: Approved (awaiting implementation tickets)

## Summary

A planning engine that keeps the Jira backlog filled so the autonomous deliver loop
always has pullable work. The AI drafts tickets and batch plans; the human stays the
gate (endorse → promote → the loop pulls).

## Role

- **Planner**: gathers signals, dedups, drafts tickets + batch plan
- **Human**: owns the roadmap doc, reviews batch plans, endorses proposals, applies
  `ai-ready` via `/promote-batch`
- **Delivery loop**: existing contract — pulls `ai-ready`, runs, PRs, merge

## Inputs (five signals)

1. **Codebase health** — scan repo for smells/gaps (e.g. the IDOR, hardcoded password,
   NPE paths already found): `lock balance read-modify-write`, `add catch-all error
   handler`.
2. **Roadmap** — read `docs/roadmap.md` (human-owned) and break near-term items into tickets.
3. **Board state** — watch Jira for carryover, blockers, repeated bug areas.
4. **R&D** — propose spikes / POCs / library evaluations as low-confidence suggestions.
5. **Team feedback** — refine raw "wouldn't it be nice" items pasted in.

## Outputs

1. **`docs/roadmap.md`** — rolling, human-owned. Planner reads it; may *suggest* entries
   via codebase-health, but the human owns content.
2. **Batch plan doc** `docs/plans/YYYY-MM-DD.md` — per sweep: theme, rationale, full
   candidate list with confidence ratings.
3. **Jira tickets** — one per endorsed near-term item, drafted with summary + description + AC.

## Authority and staging

- Planner creates tickets into To Do under a **`proposal` label** — never `ai-ready`.
- **Dedup pre-check**: skip items already covered by an existing non-Done ticket.
- **Confidence rating**:
  - codebase-health = **high** → auto-created as proposals
  - roadmap = **medium** → created as proposals
  - R&D / team-feedback = **low** → listed in plan as suggestions, not created
- Human reviews the batch plan and endorses items.

## Mechanics and cadence

- **`backlog-planner` skill** (`.opencode/skills/backlog-planner/SKILL.md`) — defines HOW:
  signals, dedup rules, ticket format, confidence scoring.
- **`/plan-backlog` command** (`.opencode/command/plan-backlog.md`) — triggers a run.
- **Weekly sweep** — cron/CI workflow invoking `/plan-backlog` headlessly (same skill).
- **`/promote-batch` command** (`.opencode/command/promote-batch.md`) — flips endorsed
  proposals from `proposal` → `ai-ready` in one step.

## Data flow

```
signals → dedup → draft plan + rate candidates → stage proposals (high/medium Conf)
→ human reviews plan → endorse → /promote-batch → ai-ready tickets → delivery loop pulls
```

## Build order

1. `backlog-planner` skill + `/plan-backlog` command (the core)
2. `/promote-batch` command
3. Scheduled weekly sweep (cron/CI)

Build and trust the manual path first; add the cron only after the manual flow produces
good proposals.

## Exclusions

- Does NOT auto-merge or auto-promote without human endorsement.
- Does NOT own the roadmap content — reads it, suggests only.
- Does NOT replace the delivery loop's review/merge gates.

## Dependencies

- Reviewer subagent (BK-32) and mentor skill (BK-25) exist to be cross-referenced.
- Child ticket under epic BK-20.
