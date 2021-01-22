package com.github.guignol.indrah.utils;

import com.github.guignol.indrah.command.ApplyPatchCommand;
import com.github.guignol.indrah.command.CommandOutput;
import com.github.guignol.indrah.model.DiffLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class ResetHelper {

    private final Runnable reload;

    public ResetHelper(Runnable reload) {
        this.reload = reload;
    }

    public void reset(Path root, List<DiffLine> selectedLines) {
        if (root == null || selectedLines == null || selectedLines.isEmpty()) {
            return;
        }
        StageUtils.stage(getExecutor(root), selectedLines, true, output -> reload.run());
    }

    @NotNull
    private ApplyPatchCommand.Executor getExecutor(final Path root) {
        return new ApplyPatchCommand.Executor() {

            @Override
            public void execute(String patch, boolean reverse, @Nullable Consumer<CommandOutput> callback) {
                if (patch == null) {
                    if (callback != null) {
                        callback.accept(CommandOutput.NULL);
                    }
                    return;
                }

                final Path patchFile = PatchUtils.writeToFile(root, patch, "___forFile");
                ApplyPatchCommand.forFile(root, patchFile, reverse)
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
