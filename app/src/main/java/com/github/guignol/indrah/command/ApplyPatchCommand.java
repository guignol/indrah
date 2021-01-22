package com.github.guignol.indrah.command;

import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ApplyPatchCommand extends Command {

    public static ApplyPatchCommand forFile(Path dir, Path patch, boolean reverse) {
        // indexではなくファイルの操作を行う
        return new ApplyPatchCommand(dir, patch, reverse, null);
    }

    // [git-bash] echo $?
    // [windows] echo %ERRORLEVEL%
    // 仕様は確認できていないが、実際に試したら普通に0だった
    // public static final int EXIT_WITH_SUCCESS = 0;

    private final Path patch;
    private final boolean reverse;
    private final String option;

    public ApplyPatchCommand(Path dir, Path patch, boolean reverse) {
        // 基本はindexの操作
        this(dir, patch, reverse, "--cached");
    }

    private ApplyPatchCommand(Path dir, Path patch, boolean reverse, String option) {
        super(dir);
        this.patch = patch;
        this.reverse = reverse;
        this.option = option;
    }

    @Override
    protected String[] command() {
        // String apply_cmd = "apply --cached --whitespace=nowarn"; // TODO
        final String patchPath = this.patch.toString();
        final List<String> commands = new ArrayList<>();
        commands.add("git");
        commands.add("apply");
        if (reverse) {
            commands.add("-R");
        }
        if (!StringUtils.isBlank(option)) {
            commands.add(option);
        }
        commands.add(patchPath);
        return commands.toArray(new String[0]);
    }

    @Override
    protected boolean isValid() {
        return super.isValid() && patch != null && Files.exists(patch);
    }

    public interface Executor {

        void execute(final String patch, boolean unstage, @Nullable Consumer<CommandOutput> callback);

        @NotNull
        Path getCurrentDirectory();
    }
}
