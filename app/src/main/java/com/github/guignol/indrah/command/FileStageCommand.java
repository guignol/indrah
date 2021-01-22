package com.github.guignol.indrah.command;

import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class FileStageCommand extends Command {

    private final String[] targets;
    private final boolean unstage;

    public FileStageCommand(Path dir, boolean unstage, @Nullable String... targets) {
        super(dir);
        this.targets = targets;
        this.unstage = unstage;
    }

    @Override
    protected String[] command() {
        final Stream<String> base;
        if (unstage) {
            base = Stream.of("git", "reset", "--");
        } else {
            base = Stream.of("git", "add", "--");
        }
        return Stream.concat(base, targets()).toArray(String[]::new);
    }

    private Stream<String> targets() {
        if (targets == null || targets.length == 0) {
            return Stream.of(".");
        } else {
            return Arrays.stream(targets).map(filePath -> {
                if (".".equals(filePath)) {
                    return filePath;
                }
                return StringUtils.doubleQuotation(filePath);
            });
        }
    }
}
