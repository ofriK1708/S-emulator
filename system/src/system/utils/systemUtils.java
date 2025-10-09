package system.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class systemUtils {

    public static void validateFile(@NotNull Path filePath) throws IOException {
        if (!filePath.getFileName().toString().endsWith(".xml")) {
            throw new IllegalArgumentException("File must be an XML file");
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("File does not exist or is not a regular file");
        }
        if (!Files.isReadable(filePath)) {
            throw new IOException("File is not readable");
        }
    }
}
