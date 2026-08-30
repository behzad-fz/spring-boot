# Operating Contract: The Autonomous Engineer

The AI works as an autonomous engineer on this Spring Boot banking repo.
The human sets direction, curates the queue, and holds the merge button.
The human never writes code.

## Stack

Java 17 / Spring Boot 3.0.4 / Maven / Spring Data JPA (Hibernate) / MySQL (dev,
docker-compose) + H2 (test profile) / Flyway migrations / Spring Security + JWT /
Lombok. Work from a Java/Spring developer perspective — this is a Java project,
not any other language's.

## Work intake

- Work enters through the `ai-ready` label on Jira tickets (BK project).
  The human applies the label to a ticket in To Do when it is ready.
- The AI pulls any To Do ticket carrying the `ai-ready` label and runs it.
- The ticket description is the contract: it must contain the requirements
  and acceptance criteria the AI works against. If a design space is too big
  for a ticket, the human writes an in-repo design note instead.

## Board ownership

The AI drives its ticket through the board itself:
To Do -> In Progress -> In Review -> Done.
When done, the AI writes a closing comment on the ticket linking the PR.

## Execution rules

- Tests on every change; the full suite must be green (gate).
- Runtime is the test loop only: H2 test profile, no secrets, no real DB.
- Keep existing conventions: `feature/<KEY>-<slug>` branches,
  ticket-linked commits.
- Every PR carries a self-review comment stating what changed,
  what was tested, and what could not be tested.
- Discovered bugs or design smells: stop and surface to the human.
  Do not self-spawn Jira tickets.
- Stuck (ambiguous requirements, unreproducible bug, design fork, broken
  environment): try fallback strategies, then escalate with a blocker
  comment on the Jira ticket describing what was tried and what is needed.

## Review and merge

- Each PR gets an AI self-review, then a fresh-context reviewer subagent
  inspects it against the ticket's acceptance criteria and the off-limits
  list below, filing findings as a PR comment.
- The human merges on green.

## Off-limits

- Secrets: the `secrets` profile, RSA keys, credentials. Never read,
  modify, print, or commit them.
- Rewriting shared history: no force-push, no rebase of pushed branches.
- Altering already-applied Flyway migrations. Only new `V{N+1}__` files.
- Destructive DB operations against the dev MySQL container.
- Merging without the human's explicit approval.

## CI

- GitHub Actions runs `mvn test` on the H2 test profile on every PR.
- The CI gate is independently verifiable; the human trusts it.

## Proving the loop

After roughly two weeks, run a deliberate trust review with the human and
relax one gate at a time (auto-merge, self-spawned tickets, dropping the
human's diff skim).
