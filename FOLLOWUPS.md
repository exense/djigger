# djigger — follow-up backlog

Follow-ups identified while upgrading the MongoDB stack and adding integration test coverage
(branches `DJIG-15` / `DJIG-16`). Grouped by theme; not in priority order.

## A. Library upgrades

- [x] **Jetty 9.4 → 12.** Done: `12.1.11` (EE10 modules `jetty-ee10-servlet`/`jetty-ee10-webapp` +
  version-aligned `jetty-server`). `ServiceServer.java` migrated to the EE10 `ServletContextHandler`/
  `WebAppContext` and the `ResourceFactory`/`setBaseResource` static-content API. The collector is now
  built at **Java 17** (`client`/`client-ui` stay 11, agent stays 8). Jetty 12.1.x also has a **Java 17**
  minimum (per the official 12.1 docs) and still ships the EE10 modules, so it's preferred over 12.0.x
  for the longer support runway; the bump from 12.0.37 → 12.1.11 required no code changes. Guarded by
  `RestServiceIT` + `WebStaticContentIT`.

  > **Support-policy decision (2026-07):** the collector moves to **Java 17**; `client`/`client-ui`
  > stay at Java 11 and the agent stays at Java 8 bytecode. **Target applications must run on a JVM ≥ 17**
  > (attach/agent). Applications *compiled* for Java 8+ bytecode remain supported as long as they run on
  > a JVM ≥ 17 (same-version attach). We therefore do **not** support attaching to Java-8-*runtime*
  > targets and do not add a cross-version (17→8) attach test.
- [x] **Jersey 2.47 → 3.1.x.** Done: `3.1.12` (Jakarta EE 10). `Services.java` moved from `javax.ws.rs`/
  `javax.servlet`/`javax.inject` to the `jakarta.*` namespaces; `javax.xml.bind:jaxb-api:2.1` replaced by
  `jakarta.xml.bind-api:4.0.5`.
- [x] **SLF4J / Logback.** Done as part of the Jetty 12 work (not deferred): Jetty 12 pulls SLF4J 2.x, so
  `slf4j-api`/`log4j-over-slf4j` → `2.0.17` and `logback-classic` → `1.5.18` (SLF4J-1.7-era logback would
  silently stop binding). Collector logging verified working (logback 1.5.18 loads `logback.xml`).
- [ ] **Remaining Dependabot PRs.** Sweep the smaller transitive bumps once this lands. `djigger-demo`
  (not in the reactor, no sources) still declares dead Jetty 9.4 deps — drop them opportunistically.

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