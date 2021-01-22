package com.github.guignol.indrah.mvvm.diff;

import com.github.guignol.indrah.model.swing.*;
import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.EventStatus;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.MouseEvent;

class DiffListViewModel extends IViewModel<DiffListModel> {

    static boolean table = true;
//    static boolean table = false;

    DiffListViewModel(DiffListModel model) {
        super(model);
        model.onIntervalSelected()
                .observeOn(SwingScheduler.getInstance())
                .forEach(interval -> selectionModel.setSelectionInterval(interval.from, interval.to));
    }

    /////////// from View

    void undo() {
        model.undo();
    }

    void toClipboard() {
        model.toClipboard();
    }

    void selectAll() {
        model.selectAll();
    }

    void select(int[] indices) {
        model.select(indices);
    }

    /////////// to View

    boolean canTrash() {
        return model.canTrash();
    }

    Observable<EventStatus> onUpdate() {
        return model.onUpdate().observeOn(SwingScheduler.getInstance()).map(data -> {

            listModel.clear();
            selectionModel.clearSelection();

            if (data == null || data.isEmpty()) {
                return EventStatus.NEXT;
            }

            data.forEach(listModel::add);
            return EventStatus.NEXT;
        });
    }

    final SimpleListModel<DiffLineForList> listModel = new SimpleListModel<>();

    private final Drag<Integer> drag = new IndexedDrag();
    private int clickedHeader;

    final MousePressReactor mouseListener = new MousePressReactor(drag::reset) {
        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            if (drag.isDragging()) {
                if (drag.from() == clickedHeader) {
                    model.clickHeader(clickedHeader);
                }
            }
            super.mouseReleased(mouseEvent);
        }
    };

    final DragSelectionModel selectionModel = new DragSelectionModel(drag,
            (fromIndex, toIndex) -> {
                final DiffLineForList from = listModel.getElementAt(fromIndex);
                final DiffLineForList to = listModel.getElementAt(toIndex);
                // ヘッダー以外の、同じhunkの行のみ同時に選択できる
                return !from.isHeader() && !to.isHeader() && from.diffLine.belongsToSameHunk(to.diffLine);
            },
            clickedIndex -> {
                final DiffLineForList clicked = listModel.getElementAt(clickedIndex);
                // headerがクリックされた場合、あるいは、ヘッダー内でドラッグされた場合
                if (clicked.isHeader()) {
                    clickedHeader = clickedIndex;
                    drag.prepare(clickedIndex);
                    // consumed
                    return true;
                } else {
                    return false;
                }
            }) {
        @Override
        public void setSelectionInterval(int fromIndex, int toIndex) {
            final Runnable setSelectionInterval = () -> {
                clickedHeader = -1;
                super.setSelectionInterval(fromIndex, toIndex);
            };
            if (table) {
                // JTableの場合、MousePressedが後に呼ばれてしまうので、setSelectionIntervalを後で呼ぶ
                SwingUtilities.invokeLater(setSelectionInterval);
            } else {
                setSelectionInterval.run();
            }
        }
    };
}
