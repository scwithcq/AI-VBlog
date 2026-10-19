package com.vblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;

/** One location for both upload writes and public resource reads. */
@Component
public class UploadStorage {
    private final Path root;
    private final Path legacy;

    public UploadStorage(@Value("${file.upload.path:}") String configuredPath) {
        Path working = Path.of("").toAbsolutePath().normalize();
        legacy = configuredPath == null || configuredPath.isBlank() ? working.resolve("uploads") : null;
        if (configuredPath != null && !configuredPath.isBlank()) {
            root = Path.of(configuredPath).toAbsolutePath().normalize();
        } else if (Files.isDirectory(working.resolve("data/uploads"))) {
            root = working.resolve("data/uploads");
        } else if (working.getParent() != null && Files.isDirectory(working.getParent().resolve("data/uploads"))) {
            root = working.getParent().resolve("data/uploads");
        } else {
            root = working.resolve("uploads");
        }
    }

    public Path root() { return root; }

    public String[] resourceLocations() {
        if (legacy != null && !legacy.equals(root) && Files.isDirectory(legacy)) {
            return new String[] { directoryUri(root), directoryUri(legacy) };
        }
        return new String[] { directoryUri(root) };
    }

    private String directoryUri(Path path) {
        String uri = path.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
