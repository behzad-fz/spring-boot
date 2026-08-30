---
description: Run a backlog-planning pass — draft a batch of Jira proposal tickets from codebase health, the roadmap, board state, R&D, and team feedback.
---

Invoke the `backlog-planner` skill and run a full planning pass. Follow the skill's
workflow exactly: load lessons from `docs/planner-feedback.md` first (binding rules),
gather all six signals (codebase health, product capability gaps, roadmap, board state,
R&D, team feedback), dedup against existing open tickets, require verbatim evidence
(file:line + quoted line) for every created candidate, score confidence, create
HIGH/MEDIUM-confidence tickets with the `proposal` label (never `ai-ready`), update
`docs/product-capabilities.md`, and write the batch plan — including its mandatory
Product perspective section — to `docs/plans/`. Report back the theme, how many tickets
were created, how many items were suggested but not ticketed, the capability gaps
observed, and anything notable.

If `$ARGUMENTS` is provided, treat it as a focus hint for this run (e.g. "transaction
layer" or "security debt") and prioritize matching candidates.
