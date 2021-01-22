package com.github.guignol.indrah.command;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;

public class DecodeImageHashCommand extends Command {

    private final String hash;

    public DecodeImageHashCommand(Path dir, String hash, Consumer<BufferedImage> imageConsumer) {
        super(dir, new CommandExecutor() {
            @NotNull
            @Override
            protected CommandOutput getOutput(Process process) throws InterruptedException {
                final ImageStreamThread image = ImageStreamThread.startWith(process.getInputStream());
                final InputStreamThread error = InputStreamThread.startWith(process.getErrorStream());
                //プロセスの終了待ち
                process.waitFor();
                imageConsumer.accept(image.get());
                return new CommandOutput(process.exitValue(), new ArrayList<>(), error.get());
            }
        });
        this.hash = hash;
    }

    @Override
    protected String[] command() {
        // "git cat-file -p " + hash,
        return new String[]{"git", "cat-file", "-p", hash};
    }
}
