package org.ulpgc.dacd;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectPaths {
    private ProjectPaths() {
    }

    public static Path resolve(String first, String... more) {
        return root().resolve(Path.of(first, more)).normalize();
    }

    private static Path root() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();

        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.isDirectory(current.resolve("EventStoreBuilder"))) {
                return current;
            }

            current = current.getParent();
        }

        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
}
