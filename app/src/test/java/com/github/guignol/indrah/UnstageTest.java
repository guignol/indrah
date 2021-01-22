package com.github.guignol.indrah;

import com.github.guignol.indrah.command.CommandOutput;
import com.github.guignol.indrah.model.DiffLine;
import com.github.guignol.indrah.model.Range;
import com.github.guignol.indrah.model.Task;
import com.github.guignol.indrah.utils.FileUtils;
import com.github.guignol.indrah.utils.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.guignol.indrah.StageTest.finder;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnstageTest {
    private static final String HORIZONTAL = "------------";
    private static final String UNSTAGE = "unstage";

    @TestFactory
    Stream<DynamicTest> unstageTestFactory() throws URISyntaxException, IOException, InterruptedException {
        final Path root = Helper.assertResource("repository/no_new_line_at_end_of_file/");
        return StreamUtils.concat(
                unstageTestFactory(new FilePair(root,
                        "before_with_linebreak.txt",
                        "after with linebreak.txt")
                        .downTo(UNSTAGE).downTo("with-with")),
                unstageTestFactory(new FilePair(root,
                        "before_with_linebreak.txt",
                        "after_without_linebreak.txt")
                        .downTo(UNSTAGE).downTo("with-without")),
                unstageTestFactory(new FilePair(root,
                        "before without linebreak.txt",
                        "after with linebreak.txt")
                        .downTo(UNSTAGE).downTo("without-with")),
                unstageTestFactory(new FilePair(root,
                        "before without linebreak.txt",
                        "after_without_linebreak.txt")
                        .downTo(UNSTAGE).downTo("without-without"))
        );
    }

    @NotNull
    private static Stream<DynamicTest> unstageTestFactory(@NotNull final FilePair filePair)
            throws InterruptedException, IOException {
        // まず、作業スペースの準備をして、diffの全体を得る
        final String target = "target.txt";
        final List<DiffLine> diffLines = prepareUnstage(filePair, target);
        // 一部stageのパターンを網羅する
        final List<Range> pattern = StageTest.pattern(diffLines);
        final Helper.NameCounter name = new Helper.NameCounter(filePair.tag);
        return pattern
                .stream()
                .map(range -> DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(filePair, target, range)));
    }

    @NotNull
    private static List<DiffLine> prepareUnstage(@NotNull final FilePair filePair,
                                                 @NotNull final String target) throws InterruptedException, IOException {
        // prepare
        FileUtils.removeFile(filePair.root);
        Files.createDirectories(filePair.root);
        Helper.assertCopy(filePair.root, filePair.before, target);
        // init, add, commit, diff
        final CountDownLatch latch = new CountDownLatch(1);
        final List<DiffLine> targetDiff = new ArrayList<>();
        Task.chain(
                GitTask.init(filePair.root, Helper::assertOutput),
                GitTask.add(filePair.root, target, Helper::assertOutput),
                GitTask.commit(filePair.root, Helper::assertOutput),
                resolver -> {
                    // ファイル修正
                    Helper.assertCopy(filePair.root, filePair.after, target);
                    resolver.resolve(CommandOutput.NEXT);
                },
                // 全てstageする
                GitTask.add(filePair.root, target, Helper::assertOutput),
                // diffを得る
                GitTask.unstage(filePair.root, finder(target), diffLines -> {
                    targetDiff.addAll(diffLines);
                    latch.countDown();
                    // dry run
                    return null;
                }, null)
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "TIMEOUT or insufficient countDown(s)");
        return targetDiff;
    }

    private static void assertUnstage(@NotNull final FilePair filePair,
                                      @NotNull final String target,
                                      @NotNull final Range range) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Task.chain(
                // 全てstage
                GitTask.add(filePair.root, target, Helper::assertOutput),
                // 一部unstage
                GitTask.unstage(filePair.root, finder(target), range::subList, output -> {
                    Helper.assertOutput(output,
                            HORIZONTAL + "unstage failed"
                                    + "\n" + "root: " + filePair.root.getFileName()
                                    + "\n" + "before: " + filePair.before
                                    + "\n" + "after: " + filePair.after
                                    + "\n" + "selected lines: from " + range.begin + " to " + range.end
                                    + "\n" + HORIZONTAL
                    );
                    latch.countDown();
                })
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "TIMEOUT or insufficient countDown(s)");
    }
}
