package com.github.guignol.indrah.mvvm.log;

import com.github.guignol.indrah.model.CommitLogAnnotated;
import com.github.guignol.indrah.model.swing.SimpleListModel;
import com.github.guignol.indrah.mvvm.arrows.VisibleCells;
import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.EventStatus;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

import javax.swing.*;
import java.beans.Transient;

class LogListViewModel extends IViewModel<LogListModel> {

    final SimpleListModel<CommitLogAnnotated> dataModel = new SimpleListModel<>();
    final DefaultListSelectionModel selection;

    LogListViewModel(LogListModel model) {
        super(model);
        selection = new DefaultListSelectionModel();
    }

    /**
     * JListのメソッドのコピー（よく分かっていないけど、複数選択があるから面倒なやつ）
     */
    @Transient
    private int[] getSelectedIndices() {
        ListSelectionModel sm = selection;
        int iMin = sm.getMinSelectionIndex();
        int iMax = sm.getMaxSelectionIndex();

        if ((iMin < 0) || (iMax < 0)) {
            return new int[0];
        }

        int[] rvTmp = new int[1 + (iMax - iMin)];
        int n = 0;
        for (int i = iMin; i <= iMax; i++) {
            if (sm.isSelectedIndex(i)) {
                rvTmp[n++] = i;
            }
        }
        int[] rv = new int[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }

    /////////////// from View

    void updateVisibleCell(VisibleCells cell) {
        model.updateVisibleCell(cell);
    }

    void toClipboard() {
        model.toClipboard(getSelectedIndices());
    }

    ////////////// to View

    Observable<EventStatus> onDataUpdated() {
        return model.onDataUpdated()
                .observeOn(SwingScheduler.getInstance())
                .map(history -> {
                    dataModel.clear();
                    history.annotated.forEach(dataModel::add);
                    return EventStatus.NEXT;
                });
    }

}
