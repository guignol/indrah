package com.github.guignol.indrah;

import com.github.guignol.indrah.model.Task;
import com.github.guignol.indrah.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnstageNewFileTest {

    @TestFactory
    Stream<DynamicTest> unstageNewFileTestFactoryWithLinebreak() throws URISyntaxException {
        final Path root = Helper.assertResource("repository/unstage_new_file/");
        final String WITH_LINEBREAK = "end_with_linebreak/with space.txt";
        final Helper.NameCounter name = new Helper.NameCounter("with line break");
        return Stream.of(
                // TODO ちゃんとパターンを網羅したほうがいい
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITH_LINEBREAK, lines -> lines.subList(2, 3), 3)),
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITH_LINEBREAK, lines -> lines.subList(0, 5), 1)),
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITH_LINEBREAK, lines -> lines.subList(0, 3), 2)),
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITH_LINEBREAK, lines -> lines.subList(4, 5), 2))
        );
    }

    @TestFactory
    Stream<DynamicTest> unstageNewFileTestFactoryWithoutLinebreak() throws URISyntaxException {
        final Path root = Helper.assertResource("repository/unstage_new_file/");
        final String WITHOUT_LINEBREAK = "end_without_linebreak/test.txt";
        final Helper.NameCounter name = new Helper.NameCounter("without line break");
        return Stream.of(
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITHOUT_LINEBREAK, lines -> lines.subList(2, 3), 3)),
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITHOUT_LINEBREAK, lines -> lines.subList(0, 5), 1)),
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITHOUT_LINEBREAK, lines -> lines.subList(0, 3), 2)),
                DynamicTest.dynamicTest(name.count(),
                        () -> assertUnstage(root, WITHOUT_LINEBREAK, lines -> lines.subList(4, 5), 2))
        );
    }

    private static void assertUnstage(final Path root,
                                      @NotNull final String relativePath,
                                      final GitTask.LineSelector selector,
                                      final int expectedCount
    ) throws IOException, InterruptedException {

        FileUtils.removeFile(root, ".git");

        // TODO 呼ばれ過ぎを検知できていない
        final CountDownLatch latch = new CountDownLatch(expectedCount);

        Task.chain(
                GitTask.init(root, Helper::assertOutput),
                GitTask.add(root, relativePath, Helper::assertOutput),
                GitTask.unstage(root, diff -> true, selector, output -> {
                    Helper.assertOutput(output);
                    // 最大3回呼ばれる
                    latch.countDown();
                })
        );

        assertTrue(latch.await(5, TimeUnit.SECONDS), "TIMEOUT or insufficient countDown(s)");
    }
}
