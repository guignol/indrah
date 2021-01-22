package com.github.guignol.indrah.utils;

import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;

public class FileUtils {

    @Nullable
    public static Path findGitDirectory(@Nullable final Path directory) {
        if (directory == null) {
            return null;
        }
        if (directory.getFileName().toString().equals(".git")) {
            if (Files.exists(directory)) {
                return directory;
            } else {
                return null;
            }
        }
        final Path child = Paths.get(directory.toAbsolutePath().toString(), ".git");
        return findGitDirectory(child);
    }


    @Nullable
    public static Path findGitParent(@Nullable final Path directory) {
        final Path gitDirectory = findGitDirectory(directory);
        if (gitDirectory == null) {
            return null;
        } else {
            return gitDirectory.getParent();
        }
    }

    @NotNull
    public static Single<Path> selectDirectory(final Component component) {
        component.setVisible(true);
        final File userHome = new File(System.getProperty("user.home"));
        final File gitTop = new File(userHome, "git");
        final File startDir = gitTop.exists() ? gitTop : userHome;
        final JFileChooser chooser = new JFileChooser(startDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        final int returnState = chooser.showOpenDialog(component);
        return FileChooser.get(chooser).map(returnState -> {
            final Path path = chooser.getSelectedFile().toPath();
            return findGitParent(path);
        });
    }

    public static class FileChooser {

        static Single<Integer> get(JFileChooser chooser) {
            return Single.create(emitter -> {
                final JDialog dialog = new JDialog();
                chooser.addActionListener(e -> {
                    if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
                        emitter.onError(new Throwable("JFileChooser.CANCEL_OPTION"));
                        dialog.setVisible(false);
                        dialog.dispose();
                    } else if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                        emitter.onSuccess(JFileChooser.APPROVE_OPTION);
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                });
                // 外部クリックで閉じる
                dialog.addWindowFocusListener(new WindowAdapter() {
                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        if (emitter.isDisposed()) {
                            return;
                        }
                        emitter.onError(new Throwable("JFileChooser.CANCEL_OPTION"));
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                });
                dialog.setAlwaysOnTop(true);
                dialog.setTitle("File Chooser");
                dialog.setModal(false);
                dialog.add(chooser);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });
        }
    }

    public static Path removeFile(Path parent, String... more) throws IOException {
        return removeFile(parent.toAbsolutePath().toString(), more);
    }

    public static Path removeFile(String first, String... more) throws IOException {
        final Path target = Paths.get(first, more);
        if (Files.isDirectory(target)) {
            if (Files.exists(target)) {
                // delete recursively
                // https://stackoverflow.com/a/35989142
                Files.walk(target, FileVisitOption.FOLLOW_LINKS)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
//                    .peek(System.out::println)
                        .forEach(File::delete);
            }
        } else {
            Files.deleteIfExists(target);
        }
        return target;
    }

    public static Path replaceFile(Path path, String content) throws IOException {
        return replaceFile(path.getParent(), path.getFileName().toString(), content);
    }

    public static Path replaceFile(Path parent, String fileName, String content) throws IOException {
        Files.createDirectories(parent);
        final Path patchFile = FileUtils.removeFile(parent, fileName);
        Files.write(
                patchFile,
                content.getBytes(StandardCharsets.UTF_8.name()),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
        return patchFile;
    }
}
