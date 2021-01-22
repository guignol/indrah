package com.github.guignol.indrah.mvvm.log;

import com.github.guignol.indrah.model.CommitLog;
import com.github.guignol.indrah.model.CommitLogAnnotated;
import com.github.guignol.indrah.model.CommitLogHistory;
import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.mvvm.arrows.VisibleCells;
import com.github.guignol.indrah.utils.ClipboardUtil;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogListModel {

    public LogListModel(Observable<CommitLogHistory> log) {
        log.subscribe(this::update);
    }

    //////////////////////

    private final PublishSubject<VisibleCells> visibleCell = PublishSubject.create();

    public Observable<VisibleCells> visibleCell() {
        return visibleCell.hide();
    }

    void updateVisibleCell(VisibleCells cell) {
        visibleCell.onNext(cell);
    }

    //////////////////

    private final ValueHolder<CommitLogHistory> onDataUpdated = ValueHolder.createDefault(CommitLogHistory.empty());

    private void update(@NotNull CommitLogHistory history) {
        onDataUpdated.put(history);
    }

    Observable<CommitLogHistory> onDataUpdated() {
        return onDataUpdated.observable();
    }

    ///// キーボード・マウス

    void toClipboard(int[] selectedIndices) {
        final StringBuilder builder = new StringBuilder();
        final List<CommitLogAnnotated> annotated = onDataUpdated.get().annotated;
        for (int selectedIndex : selectedIndices) {
            final CommitLog log = annotated.get(selectedIndex).log;
            builder.append(log.commit);
            builder.append("\n");
        }
        ClipboardUtil.copy(builder.toString());
    }
}
