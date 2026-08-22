---
description: Promote endorsed backlog-planner proposals from the `proposal` label to `ai-ready`, so the delivery loop can pull them. Only ever touches tickets carrying the `proposal` label.
---

Invoke this command to run a promotion pass for backlog-planner proposals. You are the
gate-keeper's assistant: you may ONLY change labels on tickets that currently carry the
**`proposal`** label. Never touch any other ticket, never merge, never pull a ticket into
work.

## Which tickets to promote

"Endorsed" means one of:

1. **Arguments**: `$ARGUMENTS` lists explicit ticket keys (e.g. `SCRUM-46 SCRUM-50`) —
   promote exactly those, if (and only if) each carries the `proposal` label.
2. **Batch plan endorsement**: no arguments given — read the most recent
   `docs/plans/YYYY-MM-DD.md` and look for items explicitly marked as endorsed by the
   human (e.g. a checkbox `[x]`, an "approved"/"endorse" marker, or an "Endorsed"
   section). Promote exactly those ticket keys.

If neither arguments nor endorsements are found, do NOT promote anything — report which
proposal tickets exist and stop, asking the human to either pass keys or mark the plan.

## Promotion rules

For each endorsed ticket key:

1. Fetch the ticket via the Atlassian tools and **verify it has the `proposal` label**.
   If it does not, skip it and note why (idempotency: it may already be promoted).
2. Replace the `proposal` label with `ai-ready` (the end state must have `ai-ready`
   and must not retain `proposal`).
3. Leave status, assignee, description untouched.

## Report back

Summarize: promoted (keys), skipped (keys + reason: already promoted / not a proposal /
not endorsed), and anything left untouched. If nothing was promoted, say so plainly —
an empty promotion is a valid outcome, never force one.
