package com.github.guignol.indrah.utils;

import com.github.guignol.indrah.command.ApplyPatchCommand;
import com.github.guignol.indrah.command.CommandOutput;
import com.github.guignol.indrah.model.DiffLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class StageHelper {

    private final Runnable reload;

    public StageHelper(Runnable reload) {
        this.reload = reload;
    }

    public void stage(Path root, List<DiffLine> selectedLines, boolean unstage) {
        if (root == null || selectedLines == null || selectedLines.isEmpty()) {
            return;
        }
        StageUtils.stage(getExecutor(root), selectedLines, unstage, output -> reload.run());
    }

    @NotNull
    private ApplyPatchCommand.Executor getExecutor(final Path root) {
        return new ApplyPatchCommand.Executor() {

            @Override
            public void execute(String patch, boolean unstage, @Nullable Consumer<CommandOutput> callback) {
                if (patch == null) {
                    if (callback != null) {
                        callback.accept(CommandOutput.NULL);
                    }
                    return;
                }

                final Path patchFile = PatchUtils.writeToFile(root, patch);
                new ApplyPatchCommand(root, patchFile, unstage)
                        .call(output -> {
                            //        System.out.println(output.exitValue);
                            output.standardInputs.forEach(System.out::println);
                            output.standardErrors.forEach(System.out::println);
                            if (callback != null) {
                                callback.accept(output);
                            }
                            reload.run();
                        });
            }

            @NotNull
            @Override
            public Path getCurrentDirectory() {
                return root;
            }
        };
    }
}
