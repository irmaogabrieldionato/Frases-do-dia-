# Agent instructions — Checklist Orbis Android (Claude, Codex, ChatGPT, other AIs)

> **Scope:** Only `ChecklistOrbisAndroid/**` and the linked Checklist Orbis documentation/issues in this repository. These instructions do not authorize work on other applications here.
>
> **Non-negotiable branch:** `checklist-orbis`. **Never commit to, open PR targeting, merge into, rebase onto or otherwise modify `main`.** If the active branch is not `checklist-orbis`, stop writes and move to the authorized branch; do not override branch protections.

## Start here — required every session

1. Read `../pesquisas/progresso-15-concorrentes.md` and `../pesquisas/concorrentes-checklist-orbis-2026-11-15.md`, especially latest execution.
2. Read `../pesquisas/PLANO-MESTRE-COLABORACAO-IAS-CHECKLIST-ORBIS.md`, the [multidevice media/account issue #2](https://github.com/irmaogabrieldionato/Frases-do-dia-/issues/2), and this file.
3. Inspect actual source and tests, latest HEAD and GitHub Actions run. Historical descriptions are not proof of current implementation.
4. Pick **one unclaimed atomic item** from the plan; log who owns affected files in a coordination issue/comment. One integrator commits to the shared branch at a time.
5. Make testable changes, run unit tests, build debug APK, inspect CI of **exact final SHA**, update chronological progress with real URLs. A CI run for an older commit does not validate new changes.

## Product invariants

- Proprietary, original, **offline-capable calendar**. **No Google Calendar/Google Agenda API, import, auth, sync, SDK, query, dependency, or copied UI/assets.** Study external calendars only conceptually.
- Keep applicationId/namespace `com.orbis.meudia`, preexisting preferences `meu-dia-dados-v1`, task IDs, recurrence, local dated histories, user data and compatibility with backups. No destructive migrations.
- Maintain 15 languages: `en zh-CN hi es fr ar bn ru pt-BR id ur de ja sw mr`. Arabic and Urdu require RTL. Priorities: green/blue/yellow/orange/red; percentage 0–100% per dated occurrence; filters, calendars, reminders and backup must not regress.
- AppGuide manual presently uses Portuguese/English for portions of its content: localization to 15 is a documented next step, not already done.
- Photo/video and tasks eventually belong to accounts via dedicated Supabase; local cache may support offline. **No working cloud login/sync yet.** User approved Supabase **free tier only**, but asked for **24 real independent local improvements first**. Progress now **2/24** after iterations 138 and 139. Do not use another product's `24 H2O` project. Do not add payment, subscription, paid tier, service_role, secret or keystore to repo/client.
- Never claim 15 competitors completed: order currently Microsoft To Do → Google Tasks → Any.do → Structured → Taskito → HabitNow → Habitica → Amazing Marvin → Trello → Notion Calendar → Tasks.org → Loop Habit Tracker → Super Productivity → TickTick → Todoist. The user may revise order by a newer explicit instruction.
- Code, designs, icons and texts must be original/appropriately licensed and commercially usable. Check licenses before adding dependencies.

## Code map

- `app/src/main/java/com/orbis/meudia/MainActivity.java`: UI, task editor, calendar interactions, filters, JSON backup flow. **High collision risk** — exclusive edit lease.
- `AppGuide.java`: built-in guide with priority legend; `Languages.java`: translations and locale/RTL; **high collision risk**.
- `Schedule.java`: occurrence rules; `TaskTime.java`: time overlap and conflict detection; `TaskDeadline.java`; `DailySuggestions.java`.
- `BackupRestoreCommit.java`, `TaskAttachments.java`: backup portability and attachment metadata; `LocalReminder*.java`: Android local reminders.
- `app/src/test/java/com/orbis/meudia/**`: JVM regression tests; `.github/workflows/build-checklist-orbis.yml`: CI.
- `../pesquisas/supabase-checklist-orbis-schema-proposto.sql`, `../pesquisas/supabase-checklist-orbis-testes-seguranca.sql`: proposed **unapplied** server material.

## Working protocol / shared branch concurrency

- Before editing, record: task ID, role, file lease, HEAD SHA, exact objective, test cases, dependencies and state.
- No simultaneous writes to the same file and no `force push`. Re-read HEAD and diff before every push. If another agent changed the file, reconcile and rerun tests; never overwrite blindly.
- Split large work into one user-visible improvement, minimal tests and review. Separate commits for code/tests/docs are fine but count **one** increment for one real feature.
- If bug/CI failure arises, stop advancing features, fix root cause and rerun on final SHA.
- Do not auto-merge or create PRs to main. Avoid creating new branches unless explicitly authorized. For agent collaboration, issue comments/patches can be handed to a single integrator.
- The CI workflow triggers on changes under `ChecklistOrbisAndroid/**` or workflow YAML; editing `pesquisas/**` alone does **not** trigger CI.
- For local environment (Java 17, SDK 35, Gradle 8.9), run:
  ```sh
  gradle -p ChecklistOrbisAndroid :app:testDebugUnitTest --no-daemon
  gradle -p ChecklistOrbisAndroid :app:assembleDebug --no-daemon
  ```
- CI is not physical/instrumented testing. Release signing has a separate private key gate. Record GitHub run URL, exact head_sha, status and APK artifact ID only if verified; artifacts can expire.

## Gate / report template

```md
Task: ORB-___ | Branch: checklist-orbis | HEAD before: ___
Agent/role: ___ | File lease: ___ | State: analysis/implementing/review/CI pending/validated/blocked
What changed (paths/behavior):
Test cases + actual results:
Regression check (calendar, recurrence, 15 languages, five priorities, 0-100 history, backups):
CI: URL, exact SHA, status/conclusion, job; APK ID/link or "not confirmed":
Physical device/TalkBack/release signing: done with evidence OR explicitly pending:
Privacy/license risk and blockers:
Commits/next action:
```

**Read the full [master plan](../pesquisas/PLANO-MESTRE-COLABORACAO-IAS-CHECKLIST-ORBIS.md) for the 22 remaining atomic steps (140–161), Supabase gate, competitive matrix and acceptance cases.**
