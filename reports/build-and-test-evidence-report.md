# Build and Test Evidence Report — T1.1

**Report type:** Reporting infrastructure (authorized)
**Generated:** 2026-11-09 (Europe/Lisbon)
**Author role:** Developer (reporting only — no application code or existing project documents modified)
**Method:** Evidence-only. Based strictly on artifacts found inside the project workspace. No fresh build or test execution was performed for this report.

---

## 1. Purpose

Consolidate the build and test evidence available in the project for task **T1.1 — Project bootstrap only**, and state precisely what that evidence does and does not prove.

## 2. Evidence inventory

### 2.1 Maven Surefire reports (primary test evidence)

| Artifact | Path | Last write time |
|---|---|---|
| Surefire text report | `target/surefire-reports/com.humanit.challenge.HumanItChallengeApplicationTests.txt` | 2026-11-09 10:55:14 |
| Surefire XML report | `target/surefire-reports/TEST-com.humanit.challenge.HumanItChallengeApplicationTests.xml` | 2026-11-09 10:55:14 |

Reported result (identical in both files):

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.693 s
Test set: com.humanit.challenge.HumanItChallengeApplicationTests
```

XML attributes: `tests="1" errors="0" skipped="0" failures="0" flakes="0"`, Surefire report schema version 3.0.2.

### 2.2 Environment recorded in the Surefire XML

| Property | Value |
|---|---|
| `java.version` | 21.0.12.1 (Oracle, `java.specification.version 21`) |
| JDK path | `C:\Program Files\Java\jdk-21.0.12.1` |
| OS | Windows 11 (`os.name`, version 10.0), amd64 |
| Timezone | Europe/Lisbon |
| Run timestamp | Test JVM started 2026-11-09 10:55:10 (per `sun.java.command`); test completed by 10:55:13 |
| Application banner | `Spring Boot :: (v3.5.16)` |
| Startup log | `Started HumanItChallengeApplicationTests in 1.152 seconds (process running for 2.556)` |

### 2.3 Corroborating build artifacts

| Artifact | Observation |
|---|---|
| `target/classes/com/humanit/challenge/HumanItChallengeApplication.class` | Present, last write 2026-11-09 10:55:09 — compile step succeeded for the current sources |
| `target/test-classes/` | Present — test compile succeeded |
| Source timestamps | `pom.xml` 10:41:46, `HumanItChallengeApplication.java` 10:41:47 on 2026-11-09 — the Surefire run (10:55) post-dates the latest source change, so the run corresponds to the current source state |

### 2.4 Build configuration under test

`pom.xml`: Spring Boot parent `3.5.16`, artifact `human-it-challenge 0.0.1-SNAPSHOT`, `java.version 21`, dependencies `spring-boot-starter` and `spring-boot-starter-test` (test scope) only; plugins `spring-boot-maven-plugin` and `maven-enforcer-plugin` with `requireJavaVersion [21,22)` and `requireMavenVersion [3.10.0,)`. Maven Wrapper `3.3.4` pinned to Maven `3.10.0` (`.mvn/wrapper/maven-wrapper.properties`).

## 3. What the evidence supports (T1.1 acceptance criteria)

| Acceptance criterion (per `implementation-plan.md`) | Assessment |
|---|---|
| Project builds successfully with the Maven Wrapper | **Supported** — compiled classes exist and the Surefire (test) phase executed with 0 failures/errors for the current sources |
| Application starts locally | **Partially supported** — the Spring context loaded successfully inside the `contextLoads` test (`Started HumanItChallengeApplicationTests …`). A standalone, non-test application start is **NOT EVIDENCED** |
| Java 21 baseline is enforced | **Supported** — enforcer rule in `pom.xml` plus `java.version 21.0.12.1` recorded in the run |
| No domain, persistence, security or Docker code introduced | **Supported** — source tree and dependency list inspected; nothing beyond the bootstrap scope is present |

## 4. Explicit limitation — autonomous termination is NOT proven

> **The Maven Surefire reports support the build and test results, but they do not prove autonomous application termination.**

Specifically:

- The Surefire run starts the application inside a Spring TestContext managed by JUnit; its lifecycle is controlled by the test framework, not by the application's own `main` method.
- The reports contain startup log lines but **no record of a standalone start** (e.g., `mvnw spring-boot:run` or `java -jar`), **no shutdown log lines**, and **no process exit code**.
- No console transcript, log file, or run record of a standalone start/stop exists anywhere in the project.

**Conclusion:** Autonomous application termination for T1.1 is **NOT AVAILABLE / PENDING EVIDENCE**. Proving it requires a new authorized verification run that starts the application standalone and records startup, shutdown and exit status.

## 5. Verification procedures pending execution (documented only)

The following procedures would close the evidence gaps above. They were **not executed** during this reporting task:

1. `.\mvnw.cmd test` — reproduce the test result (PENDING EVIDENCE).
2. Start the application standalone (`.\mvnw.cmd spring-boot:run` or `java -jar target/*.jar`), wait for the started log line, stop it, and capture the console transcript including shutdown lines and the process exit code (PENDING EVIDENCE for criterion "Application starts locally" and for autonomous termination).

---

**Status of this report:** Complete for the evidence available on 2026-11-09.
