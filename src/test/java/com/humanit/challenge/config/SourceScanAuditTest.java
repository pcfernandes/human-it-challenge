package com.humanit.challenge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * T-M18 — source-scan audit (inverted from the superseded Revision 2 fail-fast):
 * {@code spring.profiles.default: dev} exactly once in {@code application.yml} only;
 * zero {@code META-INF/spring.factories}; zero {@code EnvironmentPostProcessor}
 * classes; no committed {@code spring.profiles.active} anywhere (OD-01 / OD-02,
 * mandatory changes 1–3).
 */
class SourceScanAuditTest {

    private static final Pattern DEFAULT_DEV =
            Pattern.compile("(?m)^\\s*default:\\s*dev\\s*$");
    private static final Pattern PROFILES_ACTIVE_KEY =
            Pattern.compile("(?m)^\\s*active:\\s*\\S.*$");

    @Test
    void enforcesProfileMechanismAndGuardAbsences() throws IOException {
        Path projectRoot = projectRoot();
        Path resources = projectRoot.resolve("src/main/resources");
        Path sources = projectRoot.resolve("src/main/java");

        // 1. spring.profiles.default: dev committed exactly once, in application.yml.
        String applicationYml = Files.readString(resources.resolve("application.yml"));
        assertThat(DEFAULT_DEV.matcher(applicationYml).results().count())
                .as("application.yml sets the default dev profile exactly once (OD-02)")
                .isEqualTo(1);
        for (String name : List.of("application-dev.yml", "application-prod.yml")) {
            assertThat(DEFAULT_DEV.matcher(Files.readString(resources.resolve(name))).results().count())
                    .as(name + " must not declare spring.profiles.default")
                    .isZero();
        }

        // 2. No committed spring.profiles.active anywhere in resource files.
        try (Stream<Path> resourceFiles = Files.walk(resources)) {
            resourceFiles
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml")
                            || p.toString().endsWith(".properties"))
                    .forEach(file -> {
                        try {
                            assertThat(PROFILES_ACTIVE_KEY.matcher(Files.readString(file)).results().count())
                                    .as(file + " must not commit spring.profiles.active")
                                    .isZero();
                        } catch (IOException ex) {
                            throw new IllegalStateException("Cannot read " + file, ex);
                        }
                    });
        }

        // 3. No META-INF/spring.factories registration (OD-01).
        assertThat(Files.notExists(resources.resolve("META-INF/spring.factories")))
                .as("src/main/resources/META-INF/spring.factories must not exist (OD-01)")
                .isTrue();

        // 4. No EnvironmentPostProcessor / bootstrap package in main sources (OD-01).
        try (Stream<Path> sourceFiles = Files.walk(sources)) {
            sourceFiles
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(file -> {
                        try {
                            String content = Files.readString(file);
                            assertThat(content).doesNotContain("EnvironmentPostProcessor");
                            assertThat(content).doesNotContain("spring.factories");
                        } catch (IOException ex) {
                            throw new IllegalStateException("Cannot read " + file, ex);
                        }
                    });
        }
    }

    private static Path projectRoot() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (Path current = dir; current != null; current = current.getParent()) {
            if (Files.exists(current.resolve("pom.xml"))) {
                return current;
            }
        }
        throw new IllegalStateException("Project root with pom.xml not found above " + dir);
    }
}
