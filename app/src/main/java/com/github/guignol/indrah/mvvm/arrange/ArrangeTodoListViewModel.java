package com.github.guignol.indrah.mvvm.arrange;

import com.github.guignol.indrah.model.CommitLog;
import com.github.guignol.indrah.model.CommitLogAnnotated;
import com.github.guignol.indrah.model.swing.SimpleListModel;
import com.github.guignol.indrah.mvvm.arrows.VisibleCells;
import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.EventStatus;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ArrangeTodoListViewModel extends IViewModel<ArrangeTodo> {

    final DefaultListSelectionModel selection;
    final SimpleListModel<CommitLogAnnotated> dataModel;
    final TransferHandler transferHandler;
    private final AtomicInteger lastJunctionIndex = new AtomicInteger(Integer.MAX_VALUE);

    private final AtomicInteger maybeArrangedOldest = new AtomicInteger(0);

    ArrangeTodoListViewModel(ArrangeTodo model) {
        super(model);
        dataModel = new SimpleListModel<>();
        selection = new DefaultListSelectionModel() {

            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (index0 == dataModel.getSize() - 1 || lastJunctionIndex.get() <= index0) {
                    clearSelection();
                    return;
                }
                super.setSelectionInterval(index0, index1);
            }
        };
        selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transferHandler = new ListTransferHandler(this::onArranged);
    }

    private void onArranged() {
        final int oldest = maybeArrangedOldest.get();
        if (oldest == 0) {
            model.maybeArranged(new ArrayList<>());
        } else {
            model.maybeArranged(dataModel.subList(0, oldest));
        }
    }

    /////////////// from View

    void updateVisibleCell(VisibleCells cell) {
        model.updateVisibleCell(cell);
    }

    void interactiveRebase(int index) {
        // TODO 改変の無い最古のindexを得たい
        final List<CommitLog> targets = dataModel.subList(0, index).stream()
                .map(annotated -> annotated.log)
                .collect(Collectors.toList());
        final String ontoCommit = dataModel.getElementAt(index).log.commit;
        model.interactiveRebase(targets, ontoCommit);
    }

    void editMessage(int index) {
        final CommitLog commitLog = dataModel.getElementAt(index).log;
        System.out.println("double click: " + commitLog.message);
        model.editMessage(commitLog);
    }

    ////////////// to View

    Observable<EventStatus> onDataUpdated() {
        return model.onDataUpdated()
                .observeOn(SwingScheduler.getInstance())
                .map(history -> {
                    dataModel.clear();
                    maybeArrangedOldest.set(0);
                    onArranged();
                    lastJunctionIndex.set(Integer.MAX_VALUE);
                    for (int i = 0; i < history.annotated.size(); i++) {
                        final CommitLogAnnotated annotated = history.annotated.get(i);
                        if (annotated.lastJunction) {
                            lastJunctionIndex.set(i);
                        }
                        dataModel.add(annotated);
                    }
                    return EventStatus.NEXT;
                });
    }

    /**
     * 参考にしたが、いくつか間違ってるので注意
     * https://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
     */
    private class ListTransferHandler extends TransferHandler {

        private final Runnable onReOrdered;

        private ListTransferHandler(Runnable onReOrdered) {
            this.onReOrdered = onReOrdered;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (selection.isSelectionEmpty()) {
                return null;
            }
            final CommitLogAnnotated annotated = dataModel.getElementAt(selection.getMinSelectionIndex());
            return new StringSelection(annotated.log.commit);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            final JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            final int target = dl.getIndex();
            return target < dataModel.getSize()
                    && target <= lastJunctionIndex.get()
                    && info.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            if (selection.isSelectionEmpty()) {
                return false;
            }

            // Get the string that is being dropped.
            final Transferable t = info.getTransferable();
            final String data;
            try {
                data = (String) t.getTransferData(DataFlavor.stringFlavor);
                // 範囲内のコミットハッシュ文字列かどうかを見る
                if (dataModel.stream().noneMatch(annotated -> annotated.log.commit.equals(data))) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }

            int source = selection.getMinSelectionIndex();
            final CommitLogAnnotated draggedCommit = dataModel.getElementAt(source);

            final JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            final int target = dl.getIndex();
            if (target == source) {
                return false;
            }
            if (target < source) {
                // ドラッグ元のindexが変化する
                source++;
            }

            // Perform the actual import.
            if (dl.isInsert()) {
                dataModel.add(target, draggedCommit);
            }
            dataModel.remove(source);

            final int older = Math.max(target, source);
            if (maybeArrangedOldest.get() < older) {
                maybeArrangedOldest.set(older);
            }

            // 再描画
            selection.clearSelection();
            onReOrdered.run();
            return true;
        }
    }
}
