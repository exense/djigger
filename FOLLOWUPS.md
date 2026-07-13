# djigger — follow-up backlog

Follow-ups identified while upgrading the MongoDB stack and adding integration test coverage
(branches `DJIG-15` / `DJIG-16`). Grouped by theme; not in priority order.

## A. Library upgrades

- [ ] **Jetty 9.4 → 12.** Currently pinned at `9.4.57.v20241219` (last 9.4.x, on the EOL track).
  This is a *major* migration, not a bump: Jetty 12 uses the `org.eclipse.jetty.ee10.*` module layout,
  the **Jakarta** Servlet namespace (`javax.servlet` → `jakarta.servlet`), and **requires Java 17**.
  Blast radius is small (only `ServiceServer.java`). Must be done together with Jersey (below).

  > **Support-policy decision (2026-07):** the collector moves to **Java 17**; `client`/`client-ui`
  > stay at Java 11 and the agent stays at Java 8 bytecode. **Target applications must run on a JVM ≥ 17**
  > (attach/agent). Applications *compiled* for Java 8+ bytecode remain supported as long as they run on
  > a JVM ≥ 17 (same-version attach). We therefore do **not** support attaching to Java-8-*runtime*
  > targets and do not add a cross-version (17→8) attach test.
- [ ] **Jersey 2.47 → 3.1.x.** Coupled to the Jakarta move (`javax.ws.rs` → `jakarta.ws.rs`). Affects
  `Services.java` and the `jersey-container-servlet-core` / `jersey-media-json-jackson` / `jersey-hk2`
  dependencies. Do as one work package with Jetty 12.
- [ ] **Remaining Dependabot PRs.** The non-blocking bumps not folded into the Mongo work (logback
  1.2.x, slf4j 1.7.x, and smaller transitive updates). Sweep once Jetty/Jersey lands.

> A detailed migration plan for section A is tracked separately (see the DJIG-16 plan / chat).

## B. Test coverage gaps

- [x] **`ProcessAttachFacade` — untested.** Covered by `ProcessAttachIT`: launches a child JVM, attaches
  by PID, loads the agent, samples thread dumps and instruments `SampleApp.businessMethod`. Same-version
  attach (target runs on the build JVM ≥ 17); skips gracefully where the environment forbids JVM attach.
- [x] **`JstackLogTailFacade` — untested.** Covered by `JstackLogTailIT`: tails a jstack-format file and
  asserts the parsed `ThreadInfo` (thread name, state, and `SampleApp.businessMethod` stack frame).
- [x] **Web static content — untested.** Covered by `WebStaticContentIT` (GET `/djigger/index.html`),
  added to give the upcoming Jetty-12 `ResourceFactory`/`setBaseResource` change an automated oracle
  (previously only `/rest` was covered, by `RestServiceIT`).

## C. Product / robustness

- [ ] **JVM shutdown hook for graceful stop.** `Server.stop()` is now graceful (the `stopping` flag +
  drain), but `main()`/`start()` don't register a shutdown hook to call it — so a real `Ctrl-C` on the
  standalone collector doesn't get the clean path the integration tests do. Small, high-value wire-up.
- [ ] **Externalize the central test MongoDB credentials.** Host/user/password are hardcoded in the
  test sources (accepted risk for now); move to env/config.
- [ ] **Formalize the MongoDB backward-compat break.** Driver 5.x drops old-server support; breaking
  pre-4.x compatibility was accepted contingent on shipping a **new major release** (version bump +
  release note, not just code).

## D. Housekeeping

- [ ] **Document the new `database` config option** (`MongoDBParameters.database` — the auth-source /
  target-database split) in the collector config docs.
- [ ] **Confirm JUnit 4 → 5 migration completeness** — no straggler JUnit 4 tests remain now that the
  parent is on Jupiter.
- [ ] **Remove dead test `TestSerialization`** — flagged as redundant with `TestSerializationClient`.