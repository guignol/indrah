package com.github.guignol.indrah.mvvm.filename;

import com.github.guignol.indrah.model.swing.SimpleListModel;
import com.github.guignol.indrah.mvvm.dragdrop.Trackable;
import com.github.guignol.swing.rx.EventStatus;
import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

public class NameListViewModel extends IViewModel<NameListModel> {

    final SimpleListModel<String> dataModel = new SimpleListModel<>();

    public NameListViewModel(NameListModel model) {
        super(model);
    }

    /////////// from View

    void select(int[] indices) {
        model.select(indices);
    }

    void exportSelection() {
        model.exportSelection();
    }

    void trackOnDrag(Trackable trackable) {
        model.trackOnDrag(trackable);
    }

    void trackOnDrop(Trackable trackable) {
        model.trackOnDrop(trackable);
    }

    void toClipboard() {
        model.toClipboard();
    }

    /////////// to View

    Observable<EventStatus> onDataUpdated() {
        return model
                .onLinesUpdated()
                .observeOn(SwingScheduler.getInstance())
                .map(lines -> {
                    dataModel.clear();
                    lines.forEach(dataModel::add);
                    return EventStatus.NEXT;
                });
    }

    Observable<int[]> onSelectionImported() {
        return model.onSelectionImported().observeOn(SwingScheduler.getInstance());
    }

    Observable<EventStatus> onFocusRequested() {
        return model.onFocusRequested().observeOn(SwingScheduler.getInstance());
    }
}
