package com.github.guignol.indrah;

import com.github.guignol.indrah.command.CommandOutput;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Helper {

    static Path assertResource(final String pathName) throws URISyntaxException {
        final URL resource = StageTest.class.getClassLoader().getResource(pathName);
        assertNotNull(resource, "src/test/" + pathName + " must exist");
        final Path path = Paths.get(resource.toURI());
        assertTrue(Files.exists(path), "src/test/" + pathName + " must exist");
        return path;
    }

    static void assertOutput(final CommandOutput output) {
        assertTrue(output.exitCode == 0, output.toPretty());
    }

    static void assertOutput(final CommandOutput output, String message) {
        assertTrue(output.exitCode == 0, output.toPretty() + message);
    }

    static void assertCopy(final Path root,
                           final String source,
                           final String target) {
        assertNotNull(copy(root, source, target), "copy file\n from: " + source + "\n to: " + target);
    }

    static Path copy(final Path root,
                     final String source,
                     final String target) {
        try {
            final Path copied = Files.copy(Paths.get(root.toAbsolutePath().toString(), source),
                    Paths.get(root.toAbsolutePath().toString(), target),
                    StandardCopyOption.REPLACE_EXISTING);
            // git diffが検知しない場合があるためタイムスタンプを更新する
            long modified = Calendar.getInstance().getTimeInMillis();
            copied.toFile().setLastModified(modified);
            return copied;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class NameCounter {
        private final String name;
        private int count = 0;

        NameCounter(String name) {
            this.name = name;
        }

        String count() {
            return name + " " + count++;
        }
    }
}
