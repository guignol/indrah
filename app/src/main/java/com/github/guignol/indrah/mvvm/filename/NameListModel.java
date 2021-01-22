package com.github.guignol.indrah.mvvm.filename;

import com.github.guignol.indrah.model.Diff;
import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.mvvm.dragdrop.Trackable;
import com.github.guignol.indrah.utils.ClipboardUtil;
import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NameListModel {

    private static class SyncedSelection {

        private final AtomicReference<List<Diff>> selection = new AtomicReference<>();

        private SyncedSelection(NameListModel one, NameListModel another) {
            one.exportSelection.hide().subscribe(imported -> {
                another.importSelection(imported);
                selection.set(imported);
            });
            another.exportSelection.hide().subscribe(imported -> {
                one.importSelection(imported);
                selection.set(imported);
            });
        }

        private List<Diff> get() {
            final List<Diff> diffs = selection.get();
            if (diffs == null) {
                return new ArrayList<>();
            } else {
                return new ArrayList<>(diffs);
            }
        }
    }

    public static void syncSelection(NameListModel one, NameListModel another) {
        if (one == another) {
            throw new RuntimeException("same model cant sync");
        }
        final SyncedSelection syncedSelection = new SyncedSelection(one, another);
        one.syncedSelection = syncedSelection;
        another.syncedSelection = syncedSelection;
    }

    // 選択アイテムはworkspaceとindexで共通なので、
    // リロード前に持っていなかったアイテムがリロード後に存在すれば選択できるようにする
    @Nullable
    private SyncedSelection syncedSelection;

    ////// full data

    private final ValueHolder<List<Diff>> onDataUpdated = ValueHolder.createDefault(new ArrayList<>());

    public void update(List<Diff> data) {
        onDataUpdated.put(data);
        // 選択し直す
        importSelection();
    }

    ////// lines

    Observable<List<String>> onLinesUpdated() {
        return onDataUpdated
                .observable()
                .map(diffs -> diffs.stream().map(NameListModel::toFileName).collect(Collectors.toList()));
    }

    private static String toFileName(Diff diff) {
        return diff.summary.names.any();
    }

    ////// import selection 外部から選択の変更を受けとる

    private final PublishSubject<int[]> onSelectionImported = PublishSubject.create();

    Observable<int[]> onSelectionImported() {
        return onSelectionImported.hide();
    }

    private void importSelection() {
        importSelection(syncedSelection == null ? new ArrayList<>() : syncedSelection.get());
    }

    private void importSelection(List<Diff> imported) {
        final List<Diff> rowData = onDataUpdated.get();
        final int[] indices = ListUtils.filteredIndices(rowData, imported, Diff::refersToSameFile).toArray();
        onSelectionImported.onNext(indices);
        // Diffを更新する
        publishSelection();
    }

    ///// export selection 現在の選択を外部に伝える

    private final PublishSubject<List<Diff>> exportSelection = PublishSubject.create();

    void exportSelection() {
        exportSelection.onNext(getSelectedDiffs());
        publishSelection();
    }

    ///// selection 現在の選択をDiffリストに伝える

    public Observable<List<Diff>> showDiffs() {
        return selection.hide();
    }

    private final PublishSubject<List<Diff>> selection = PublishSubject.create();

    private void publishSelection() {
        selection.onNext(getSelectedDiffs());
    }

    ///// selection 実際に選択されているインデックス

    private final ValueHolder<int[]> selectedIndices = ValueHolder.createDefault(new int[0]);

    void select(int[] indices) {
        selectedIndices.put(indices);
    }

    public List<Diff> getSelectedDiffs() {
        final List<Diff> rowData = onDataUpdated.get();
        return Arrays.stream(selectedIndices.get())
                .mapToObj(rowData::get)
                .collect(Collectors.toList());
    }

    ///// ドラッグ

    private final PublishSubject<Trackable> onDrag = PublishSubject.create();

    public Observable<Trackable> onDrag() {
        return onDrag.hide();
    }

    void trackOnDrag(Trackable trackable) {
        onDrag.onNext(trackable);
    }

    ///// ドロップ

    private final PublishSubject<Trackable> onDrop = PublishSubject.create();

    public Observable<Trackable> onDrop() {
        return onDrop.hide();
    }

    void trackOnDrop(Trackable trackable) {
        onDrop.onNext(trackable);
    }

    ///// フォーカス

    private final EventStatus.Publisher onFocusRequested = EventStatus.create();

    Observable<EventStatus> onFocusRequested() {
        return onFocusRequested.asObservable();
    }

    public void requestFocus() {
        onFocusRequested.onNext();
    }

    ///// キーボード

    void toClipboard() {
        final StringBuilder builder = new StringBuilder();
        final List<Diff> rowData = onDataUpdated.get();
        for (int selectedIndex : selectedIndices.get()) {
            final Diff diff = rowData.get(selectedIndex);
            builder.append(toFileName(diff));
            builder.append("\n");
        }
        ClipboardUtil.copy(builder.toString());
    }
}
