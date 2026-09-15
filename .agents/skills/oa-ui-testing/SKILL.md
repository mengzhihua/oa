---
name: oa-ui-testing
description: Run browser-based OA golden paths with isolated demo data, role switching, payroll dependencies, and OAuth callback evidence.
---

# OA browser testing

Use the repo blueprint for dependency installation, local service startup and demo
accounts. The frontend is on 5176 and backend on 8086. H2 is file-backed, so ask for
permission before deleting backend/data: this resets all previous test state.
Do not reset between role switches in one workflow.

## Devin Secrets Needed

None for the local seeded demo environment. Do not reuse demo credentials against
real deployments; use separately authorized accounts there.

## Runtime procedure

- Start Maven and Vite using shell tools. Test business actions through the UI,
  maximizing the browser and recording only after setup.
- Switch roles through avatar > 退出登录. Employee, manager, HR and finance are
  separate steps in leave-to-payroll flows; preserve data across these steps.
- For leave approvals, record initial balance, approve one request and reject a
  second, then verify employee status, exact balance delta and actual approver.
- Select an unprocessed payroll month. Attempt calculation before locking
  attendance, then generate/confirm/lock as HR and continue as finance. If a
  workaround is needed, preserve the original failure in the report.
- Employee hire generates the username from employeeNo. In the demo implementation,
  initial password is the last six characters of employeeNo; check current
  HrController behavior before relying on this. Verify automatic login before
  departure, and record generated test IDs for cleanup.
- Check available meeting rooms before planning conflict coverage. If the seed
  provides none and no room-management UI exists, report the missing prerequisite;
  do not silently create fixtures through privileged APIs.
- Read OAuth client ID and exact registered callback from System > OAuth 客户端.
  Use response_type=code, scope=openid%20profile and a known state. Logged-out
  authorization may first show consent and request login only after clicking agree.
  Verify the post-login consent and final address-bar code/state. A stopped
  receiver app may produce connection-refused after a valid OA redirect; distinguish
  that from failure to issue the code.
- Test notification destination links as well as unread counters. Hidden menus do
  not establish route authorization: separately record direct-route behavior when
  role isolation is in scope.
- When typing a bare root URL into Chrome, press Delete to remove inline history
  completion before Enter, or visually confirm the full address. Incognito may
  still suggest existing history; distinguish autocomplete from app redirects.
- With multiple worktrees, match each service PID/cwd/port before stopping it.
  If the PR branch is checked out elsewhere, use the exact remote commit in detached
  HEAD rather than moving the other worktree. Coordinate exclusive browser usage.
- New appointment dialogs may retain old values. Use Ctrl+A before replacing dates
  and title, then screenshot the filled form before saving a conflict scenario.
- Compare schedule times in both the calendar and dashboard; compare approval
  trails from both attendance requests and the workflow center. Shared-looking
  components can receive different data shapes at different entry points.
- A successful notice-save toast is not publication proof: confirm the draft remains
  selectable, publish it, then switch employee accounts for read/unread assertions.
- Current demo seed may omit employee contracts. If needed, create one through
  Contract Management and separately report seed coverage versus detail rendering.

Upload screenshots before embedding them in a written report; keep local screenshot
and recording paths in the handoff. Mark every blocked or unexecuted assertion
explicitly, and do not equate nonempty payroll data with audited salary formulas.
