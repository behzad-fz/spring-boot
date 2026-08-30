# Planner Feedback — Binding Lessons

Every entry here is a binding rule for future backlog-planning runs. The planner must
read this file FIRST and apply every lesson while gathering and judging candidates.

Add an entry whenever a proposal is corrected, rejected, or right-sized by the human:
what went wrong, and the rule that prevents it.

---

## 2026-08-19 — SCRUM-52 correction

**What went wrong:** The planner claimed `CustomLocalDateDeserializer` "silently
swallows parse exceptions." False — the code wraps and rethrows as IOException.

**Rule:** Before claiming exception-handling behavior (swallowed / silent / lost),
read the exact catch block and trace where the exception goes. Quote the wrap/rethrow
in evidence. "Catches broadly" and "swallows" are different claims — verify which one
holds.

## 2026-08-19 — SCRUM-53 correction

**What went wrong:** The planner called `UserController.findUserById` a security leak
("exposes principal name"). It returns the authenticated caller's own name back to
themselves — no exposure. The real issue was that the endpoint is a stub.

**Rule:** Before calling something a leak/exposure, trace whose data is returned TO
whom. Returning the caller's own data to themselves is not an exposure. Verify the
data flow end-to-end before using security framing.
