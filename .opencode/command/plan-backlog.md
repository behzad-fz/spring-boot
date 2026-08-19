---
description: Run a backlog-planning pass — draft a batch of Jira proposal tickets from codebase health, the roadmap, board state, R&D, and team feedback.
---

Invoke the `backlog-planner` skill and run a full planning pass. Follow the skill's
workflow exactly: gather all five signals, dedup against existing open tickets, score
confidence, create HIGH/MEDIUM-confidence tickets with the `proposal` label (never
`ai-ready`), and write the batch plan doc to `docs/plans/`. Report back the theme, how
many tickets were created, how many items were suggested but not ticketed, and anything
notable.

If `$ARGUMENTS` is provided, treat it as a focus hint for this run (e.g. "transaction
layer" or "security debt") and prioritize matching candidates.
