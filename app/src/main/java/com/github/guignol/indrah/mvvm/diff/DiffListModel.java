package com.github.guignol.indrah.mvvm.diff;

import com.github.guignol.indrah.model.*;
import com.github.guignol.indrah.model.swing.DiffLineForList;
import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.utils.*;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DiffListModel {

    private final Supplier<Path> directory;
    private final StageHelper stageHelper;
    @Nullable
    private final ResetHelper resetHelper;
    public final boolean unstage;
    private final Runnable loader;

    public DiffListModel(Supplier<Path> directory, @NotNull final Runnable loader, final boolean unstage) {
        this.directory = directory;
        this.loader = loader;
        this.unstage = unstage;
        this.stageHelper = new StageHelper(loader);
        this.resetHelper = unstage ? null : new ResetHelper(loader);
    }

    @Nullable
    public Path directory() {
        return directory.get();
    }

    public void reload() {
        loader.run();
    }

    ///////////// selection 選択されているインデックス

    private final ValueHolder<int[]> selectedIndices = ValueHolder.createDefault(new int[0]);

    void select(int[] indices) {
        selectedIndices.put(indices);
    }

    private void ifSelected(Consumer<int[]> consumer) {
        final int[] indices = selectedIndices.get();
        if (!ArrayUtils.isBlank(indices)) {
            consumer.accept(indices);
        }
    }

    /////////////

    void clickHeader(int clickedHeader) {
        final List<DiffLineForList> data = onUpdate.get();
        final DiffLineForList clicked = data.get(clickedHeader);
        final Hunk targetHunk = clicked.diffLine.hunk;
        final List<DiffLine> selectedDiffs = selectedDiffs(selectedIndices.get(), data);
        // 何も選択していない場合
        if (selectedDiffs == null
                // 別のhunkを選択している場合
                || !Objects.equals(selectedDiffs.get(0).hunk, targetHunk)
                // contextのみ選択している場合
                || StageUtils.nothingToDo(selectedDiffs)
                ) {
            // このhunkの全てをstageする
            updateIndex(allDiffsForHunk(targetHunk, data));
        } else {
            // 選択中のdiffをstageする
            updateIndex(selectedDiffs);
        }
    }

    private void updateIndex(List<DiffLine> diffLines) {
        stageHelper.stage(directory(), diffLines, unstage);
    }

    void undo() {
        if (resetHelper != null) {
            ifSelected(indices -> {
                final List<DiffLineForList> data = onUpdate.get();
                resetHelper.reset(directory(), selectedDiffs(indices, data));
            });
        }
    }

    boolean canTrash() {
        return !unstage;
    }

    ///////////// リスト表示用のデータ作成

    private final ValueHolder<List<DiffLineForList>> onUpdate = ValueHolder.create();

    public void update(List<Diff> data) {
        onUpdate.put(convert(data));
    }

    Observable<List<DiffLineForList>> onUpdate() {
        return onUpdate.observable();
    }

    private List<DiffLineForList> convert(List<Diff> data) {
        final Function<Diff, ImageHolder> imageFactory = diff -> ImageHolderFactory.get(directory(), diff, unstage);
        final List<DiffLineForList> list = new ArrayList<>();
        data.forEach(diff -> {
            // 新規追加された空ファイル、または、ファイル削除、リネーム、バイナリ
            if (diff.hunks.isEmpty()) {
                // ヘッダーとダミーのコンテンツが同じhunkに属することを示せるようにするため
                final Hunk dummyHunk = Hunk.empty();
                final String multiLine = Arrays.stream(diff.header.split("\n")).collect(StringUtils.toHtml);
                final DiffLine headerLine = new DiffLine(diff, dummyHunk, -1, multiLine);
                list.add(new DiffLineForList(headerLine, true));
                // no contents
                final DiffLine noLine = new DiffLine(diff, dummyHunk, -1, diff.noHunkMessage());
                final ImageHolder imageHolder = imageFactory.apply(diff);
                list.add(new DiffLineForList(noLine, false) {
                    @Override
                    public ImageHolder imageHolder() {
                        return imageHolder;
                    }
                });
                return;
            }
            diff.hunks.forEach(hunk -> {
                final String allHeaders = diff.header + hunk.header;
                final String multiLine = Arrays.stream(allHeaders.split("\n")).collect(StringUtils.toHtml);
                final DiffLine headerLine = new DiffLine(diff, hunk, -1, multiLine);
                list.add(new DiffLineForList(headerLine, true));

                ListUtils.forEach(hunk.lines, (line, index) -> {
                    final DiffLine hunkLine = new DiffLine(diff, hunk, index, line);
                    final DiffLineForList segmented = new DiffLineForList(hunkLine, false);
                    list.add(segmented);
                });
            });
        });
        return list;
    }

    ///////////// キーボード・マウス

    void toClipboard() {
        final StringBuilder builder = new StringBuilder();
        final List<DiffLineForList> data = onUpdate.get();
        for (int selectedIndex : selectedIndices.get()) {
            builder.append(data.get(selectedIndex).item());
            builder.append("\n");
        }
        ClipboardUtil.copy(builder.toString());
    }

    void selectAll() {
        final List<DiffLineForList> data = onUpdate.get();
        ifSelected(indices -> {
            // すべて同じhunkに属する前提
            final DiffLine target = data.get(indices[0]).diffLine;
            final Predicate<DiffLineForList> sameHunk = element -> !element.isHeader() && target.belongsToSameHunk(element.diffLine);
            final int from = ListUtils.findIndex(data, sameHunk);
            final int to = ListUtils.findLastIndex(data, sameHunk);
            onIntervalSelected.onNext(new Interval(from, to));
        });
    }

    private final PublishSubject<Interval> onIntervalSelected = PublishSubject.create();

    Observable<Interval> onIntervalSelected() {
        return onIntervalSelected.hide();
    }

    /////////////

    private List<DiffLine> selectedDiffs(int[] indices, List<DiffLineForList> data) {
        if (data.isEmpty()) {
            return null;
        }
        if (indices.length == 0) {
            return null;
        }
        final int startIndex = indices[0];
        final int endIndex = indices[indices.length - 1]; // 含む

        // 選択範囲
        return data.subList(startIndex, endIndex + 1)
                .stream()
                .map(diff -> diff.diffLine)
                .collect(Collectors.toList());
    }

    private static List<DiffLine> allDiffsForHunk(Hunk target, List<DiffLineForList> data) {
        return data.stream()
                // リストのheaderは含めない
                .filter(diff -> !diff.isHeader())
                .filter(diff -> diff.diffLine.belongsToSameHunk(target))
                .map(diff -> diff.diffLine).collect(Collectors.toList());
    }

    public static class Interval {
        public final int from;
        public final int to;

        public Interval(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

}
