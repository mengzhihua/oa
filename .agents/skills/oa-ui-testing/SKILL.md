---
name: oa-ui-testing
description: Additional regression safeguards for departure dates, payslip views, and OAuth ownership.
---

# Suggested additions to existing oa-ui-testing

Merge these into the repository's existing skill rather than creating a second skill.

## Devin Secrets Needed

None for seeded local demo testing. Read current client names/grants and demo
secret initialization from the checked-out revision; never assume production
credentials equal demo credentials.

## Runtime safeguards

- After closing a drawer, let its overlay disappear before opening the avatar
  menu. Verify the login page before entering another account and verify the
  visible account name afterward.
- A payroll DTO with itemDetails does not prove every UI uses it. Check both
  employee payslips and HR detail drawers for localized allowance/adjustment rows.
- For departure payroll tests, capture the filled date prompt, then compare the
  persisted date and regenerated attendance days. Use a date different from today
  so an endpoint that defaults to today cannot produce a false pass.
- Before testing OAuth revocation ownership, introspect the fresh token and
  require active=true. A token already inactive cannot establish whether a foreign
  client's revoke was ignored. If another supported grant is needed to isolate
  ownership, report the original grant failure separately.
- A CSV downloaded before monthly generation may contain only headers. Distinguish
  successful export access/download from verification of populated exported rows.
- Read-only row-action coverage requires populated rows. A 404-backed empty list
  is not proof that edit/delete buttons are correctly hidden.
