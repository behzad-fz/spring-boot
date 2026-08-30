# Product Capabilities — Implemented Inventory

Maintained by the backlog planner (updated when flows ship or are removed). Used for
capability-gap analysis: implemented vs what a banking customer should expect.

## What customers can do today

| Capability | Where |
|-----------|-------|
| Register / login as user | `AuthController` (/api/v1/auth) |
| Login as customer, update own credentials | `CustomerAuthController` |
| Create customers (user/admin only) | `CustomerController` |
| Search customers | `CustomerController.search` |
| Update / delete customers | `CustomerController` |
| Manage customer addresses | `CustomerAddressController` |
| Create accounts (per customer), list by customer | `AccountController` |
| Change account status | `AccountController.updateStatus` |
| List own accounts; view one owned account | `MyAccountController` |
| Deposit / withdraw / payment / transfer (single-account tx) | `TransactionController` |
| Transaction history per account (ordered) | `TransactionController GET` |
| Pay a saved recipient by IBAN | `TransactionController /recipient-payment` |
| Two-leg currency conversion between own accounts | `TransactionController /currency-conversion` |
| Atomic transfer between own accounts (same currency) | `TransactionController /transfer` |
| Schedule a future withdrawal (one-off) | `TransactionController /scheduled` + scheduler |
| Manage recipients (CRUD) | `RecipientController` |

## Known gaps (candidates for feature proposals)

- No account statements (periodic or on-demand document/export)
- No transaction search / filtering / pagination beyond full ordered history
- Account-activation email on status change to ACTIVE (`AccountActivatedListener`) — the only notification; no other notification types exist
- No overdraft policy / credit limit concept
- No interest accrual
- Only one-off scheduled withdrawals — no recurring payments / standing orders
- No spending categories / budgets
- No balance snapshot / history over time (only current balance + last-transaction date)
- Recipient payments don't validate IBAN format
- No way to close an account end-to-end

(Planner: keep this section current. New gap = candidate feature proposal.)
