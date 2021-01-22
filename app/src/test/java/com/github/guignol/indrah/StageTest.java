package com.github.guignol.indrah;

import com.github.guignol.indrah.command.CommandOutput;
import com.github.guignol.indrah.model.Diff;
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
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO いろいろ変更したときに、そのときのレポジトリの状態でランダムあるいは網羅的にテストしたい
class StageTest {

    private static final String HORIZONTAL = "------------";
    private static final String STAGE = "stage";

    @TestFactory
    Stream<DynamicTest> stageTestFactory() throws URISyntaxException, IOException, InterruptedException {
        final Path root = Helper.assertResource("repository/no_new_line_at_end_of_file/");
        return StreamUtils.concat(
                stageTestFactory(new FilePair(root,
                        "before_with_linebreak.txt",
                        "after with linebreak.txt")
                        .downTo(STAGE).downTo("with-with")),
                stageTestFactory(new FilePair(root,
                        "before_with_linebreak.txt",
                        "after_without_linebreak.txt")
                        .downTo(STAGE).downTo("with-without")),
                stageTestFactory(new FilePair(root,
                        "before without linebreak.txt",
                        "after with linebreak.txt")
                        .downTo(STAGE).downTo("without-with")),
                stageTestFactory(new FilePair(root,
                        "before without linebreak.txt",
                        "after_without_linebreak.txt")
                        .downTo(STAGE).downTo("without-without"))
        );
    }

    @NotNull
    private static Stream<DynamicTest> stageTestFactory(@NotNull final FilePair filePair)
            throws InterruptedException, IOException {
        // まず、作業スペースの準備をして、diffの全体を得る
        final String target = "target.txt";
        final List<DiffLine> diffLines = prepareStage(filePair, target);
        // 一部stageのパターンを網羅する
        final List<Range> pattern = pattern(diffLines);
        final Helper.NameCounter name = new Helper.NameCounter(filePair.tag);
        return pattern
                .stream()
                .map(range -> DynamicTest.dynamicTest(name.count(),
                        () -> assertStage(filePair, target, range)));
    }

    @NotNull
    private static List<DiffLine> prepareStage(@NotNull final FilePair filePair,
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
                // diffを得る
                GitTask.stage(filePair.root, finder(target), diffLines -> {
                    targetDiff.addAll(diffLines);
                    latch.countDown();
                    // dry run
                    return null;
                }, null)
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "TIMEOUT or insufficient countDown(s)");
        return targetDiff;
    }

    private static void assertStage(@NotNull final FilePair filePair,
                                    @NotNull final String target,
                                    @NotNull final Range range) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Task.chain(
                // 全てunstage
                GitTask.reset(filePair.root, target, Helper::assertOutput),
                // 一部stage
                GitTask.stage(filePair.root, finder(target), range::subList, output -> {
                    Helper.assertOutput(output,
                            HORIZONTAL + "stage failed"
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

    @NotNull
    static Predicate<Diff> finder(final String target) {
        return diff -> diff.summary.names.any().equals(target);
    }


    @NotNull
    static List<Range> pattern(@NotNull final List<DiffLine> original) {
        // 選択のパターンを生成する
        final ArrayList<Range> pattern = new ArrayList<>();
        Step.down(original, (offset, subList) -> {
            for (int i = 0; i < subList.size(); i++) {
                final DiffLine diffLine = subList.get(i);
                if (diffLine.line.startsWith("+") || diffLine.line.startsWith("-")) {
                    pattern.add(new Range(offset, offset + i));
                }
            }
        });
        return pattern;
    }
}