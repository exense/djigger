# djigger-integration-tests

End-to-end integration tests that start a real collector (`io.djigger.collector.server.Server`),
launch target JVMs (with the djigger `-javaagent` or with JMX enabled), and assert that thread
dumps, instrumentation events and MBean metrics are stored into MongoDB.

The tests run under the maven-failsafe-plugin (class name suffix `*IT`) and are executed during the
`verify` phase:

```
mvn -f parent/pom.xml -pl djigger-integration-tests -am verify
```

They connect to the central test MongoDB by default and each test is **skipped** (JUnit assumption)
when the database or the built agent jar is not reachable. Overridable via system properties:
`-Dmongo.host -Dmongo.port -Dmongo.db -Dmongo.user -Dmongo.password` and `-Dagent.jar` (the build
copies the shaded java-agent to `target/agent.jar`).

## Gotcha: launch agent targets with a *minimal* classpath

When starting a target application that is monitored via the djigger `-javaagent`, launch it with
**only its own code on the classpath** — not the full test classpath. Use
`JvmLauncher.codeSourceOf(SampleApp.class)` (which returns just the location `SampleApp` was loaded
from) rather than `JvmLauncher.launch(mainClass, jvmArgs, appArgs)` (which reuses the full classpath).

Why: the java-agent is shaded (`javassist` → `shaded.javassist`, `org.bson` → `shaded.org.bson`,
etc.). A real monitored application never has djigger's *unshaded* jars on its classpath, so
instrumented classes resolve djigger types exclusively from the shaded agent jar. If the target is
launched with the full test classpath, djigger's unshaded jars collide with the agent's shaded copies
and instrumentation fails at runtime with e.g.:

```
NoSuchMethodError: InstrumentSubscription.isRelatedToClass(shaded.javassist.CtClass)
```

(the shaded `ClassTransformer` calls the method with a `shaded.javassist.CtClass`, but the unshaded
`InstrumentSubscription` from the test classpath expects an unshaded `javassist.CtClass`). This is a
**test-environment artifact only** — it does not affect real deployments, where targets never carry
djigger's unshaded jars — so the fix belongs in the test launcher, not the product.