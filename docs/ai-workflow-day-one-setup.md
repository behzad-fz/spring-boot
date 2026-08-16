# Day-One Setup: The Autonomous Engineer Workflow

This guide takes the AI-as-autonomous-engineer loop from this training repo into a real
job's daily routine. It's a checklist — do the setup once, then the daily job shrinks to
"curate the queue, skim PRs, merge."

---

## Part 0 — Prerequisites

- [ ] **opencode** installed on the work machine
- [ ] **`gh` (or your VCS CLI) on `PATH`** — verify with `which gh` in a fresh shell.
      This was the #1 friction point in training: `gh` existed but wasn't on PATH, so
      every PR command failed. Fix the shell profile once.
- [ ] **Git auth works non-interactively** — `gh auth status` shows "Logged in".
      Avoid credential prompts that hang headless sessions (macOS keychain prompts are
      a known hang — pre-approve or use a token).
- [ ] **The toolchain the repo needs is installable and reproducible** — e.g. Java 17.
      If the machine has a newer JDK and the project needs 17, the AI will fight Lombok
      silently. Document the correct `JAVA_HOME` in the repo's runbook.

## Part 1 — Repo setup (one time, ~30 min)

- [ ] Copy `AGENTS.md` into the repo root (use the template in this folder).
- [ ] Edit the repo-specific bits:
      - project / module names
      - who holds the merge button (usually you)
      - which CI workflow is the gate
      - any repo conventions the AI must preserve
- [ ] **Write the off-limits list for THIS repo.** Be specific:
      - which credentials / secret profiles the AI may never read or print
      - which DBs / tables are hands-off (prod, destructive ops)
      - the migration policy (new `V{N+1}` files only)
      - force-push / history-rewrite prohibition
      - the merge rule (no merge without explicit human approval — until relaxed)
- [ ] Ensure the repo's runbook (`README`) documents local setup so the AI can build
      and test without asking.

## Part 2 — Board setup (one time)

- [ ] Jira project exists (or pick the team's existing one).
- [ ] Create the **`ai-ready`** label.
- [ ] Confirm the board workflow matches: To Do → In Progress → In Review → Done.
      (The AI drives its ticket through these itself.)
- [ ] Wire the **Atlassian MCP** into `opencode.json` so the AI can read/create/comment
      on tickets and drive the board.

## Part 3 — CI: the non-negotiable gate

- [ ] CI exists and runs the test suite on every PR.
- [ ] If CI does **not** exist: the AI's first ticket is building it (this is exactly
      what SCRUM-12 did in training). Nothing else should be pulled until CI is green.
- [ ] The AI's local test run and CI must agree. If they disagree (e.g. Java version,
      profile), fix the toolchain mismatch before trusting the gate.

## Part 4 — The daily routine

```
You:  write/approve ticket → apply ai-ready label
AI:   pulls ticket → In Progress → writes code + tests → opens PR
      → self-review comment → In Review → CI runs
You:  skim PR + CI → merge → ticket auto-closes
AI:   closing comment linking the PR
```

- [ ] You never write code.
- [ ] You never ask for status — the board and PR comments are the status.
- [ ] Your review is: CI green + skim the diff + read the self-review comment.
      Don't full-review every PR; that's what the fresh-context reviewer subagent is for.

## Part 5 — The ramp (how to earn trust before the stakes get real)

- [ ] **Week 1: safe territory only.** Tickets limited to tests, refactors, bug fixes,
      low-risk internal services. Never the money-movement / production-critical paths.
- [ ] Keep the human-merge gate + full-diff skim for the first ~2 weeks.
- [ ] **After ~2 weeks**, run the trust review:
      - count merged PRs, rework found in review, bugs caught by review
      - if clean, relax ONE gate at a time:
        1. drop the human's diff skim (trust CI + self-review)
        2. allow the AI to self-spawn Jira tickets for discoveries
        3. auto-merge on green
- [ ] Once trusted, expand ticket scope to bigger features — still one feature per ticket.

## Part 6 — Gotchas learned in training (save yourself the pain)

- **`gh` not on PATH** — `which gh` in a fresh shell; add to `.zshrc`/`.bashrc`.
- **Keychain prompts hang headless runs** — pre-authorize `security` or use a token.
- **JDK mismatch breaks builds silently** — Lombok fails to compile on newer JDKs with
  `cannot find symbol` everywhere. Pin and document the correct JDK.
- **`/tmp` cleanups wipe downloaded toolchains** — don't rely on `/tmp` for the JDK;
  install it properly.
- **Stacked branches conflict** — when two PRs touch the same files, merge one first
  and forward-merge `master` into the other (never rebase pushed branches).
- **Secrets profile is gitignored** — the AI works in the test loop (H2) only; real-DB
  smoke tests are the human's job.

## Part 7 — What's different in a real job (be honest)

- Real repos have real secrets, real reviewers, real review culture, and teammates who
  will see the AI committing. The contract handles the first two; the last two are
  people problems you manage with the ramp — small safe PRs before anyone notices.
- Company conventions may conflict with the AI's defaults — put them in `AGENTS.md`.
- Multi-contributor repos: the AI must not step on in-flight work; the queue label
  (`ai-ready`) is what keeps it from grabbing tickets the team isn't ready for.
