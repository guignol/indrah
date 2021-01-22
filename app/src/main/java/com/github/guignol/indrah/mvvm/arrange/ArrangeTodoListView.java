package com.github.guignol.indrah.mvvm.arrange;

import com.github.guignol.indrah.model.CommitLogAnnotated;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.mvvm.arrows.VisibleCells;
import com.github.guignol.indrah.mvvm.log.LogListView;
import com.github.guignol.indrah.view.list.UpperListContainer;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.binding.Keys;
import com.github.guignol.swing.binding.Property;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ArrangeTodoListView implements ComponentHolder, IView<ArrangeTodoListViewModel> {

    @Override
    public void bind(ArrangeTodoListViewModel viewModel) {
        JList<CommitLogAnnotated> list = listContainer.list;
        list.setModel(viewModel.dataModel);
        list.setSelectionModel(viewModel.selection);
        list.setTransferHandler(viewModel.transferHandler);

        viewModel.onDataUpdated().subscribe(eventStatus -> {
            list.revalidate();
            list.repaint();
            listContainer.alignBottom();
        });

        final Function<Point, Integer> shouldShowUp = point -> {
            final int[] indices = list.getSelectedIndices();
            if (indices.length == 1) {
                final int index = indices[0];
                final Rectangle cellBounds = list.getCellBounds(index, index);
                if (cellBounds.contains(point)) {
                    return index;
                }
            }
            return -1;
        };
        interactiveRebase(new Keys.Registry(list), shouldShowUp)
                .subscribe(viewModel::interactiveRebase);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Rectangle r = list.getCellBounds(list.getFirstVisibleIndex(), list.getLastVisibleIndex());
                if (r != null && r.contains(e.getPoint())) {
                    if (e.getClickCount() == 2) {
                        // Double-click detected
                        int index = list.locationToIndex(e.getPoint());
                        viewModel.editMessage(index);
                    }
                }
            }
        });

        // スクロールを検知
        Property.onChanged(listContainer.scrollPane.getViewport())
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .subscribe(eventStatus -> viewModel.updateVisibleCell(VisibleCells.fromListView(list)));
    }

    private final UpperListContainer<CommitLogAnnotated> listContainer;

    ArrangeTodoListView() {
        listContainer = new UpperListContainer<>(LogListView.CellRenderer.CELL_HEIGHT);
        listContainer.list.setCellRenderer(new LogListView.CellRenderer(false));
        listContainer.list.setDragEnabled(true);
        listContainer.list.setDropMode(DropMode.INSERT);
    }

    @Override
    public Component getComponent() {
        return listContainer.getComponent();
    }

    private static Observable<Integer> interactiveRebase(Keys.Registry keysRegistry,
                                                         Function<Point, Integer> shouldShowUp) {
        final PublishSubject<Integer> publisher = PublishSubject.create();
        keysRegistry.onRightClick().subscribe(e -> {
            // ポップアップメニューを表示する
            final JComponent c = (JComponent) e.getSource();
            final Integer index = shouldShowUp.apply(e.getPoint());
            if (0 < index) {
                final JPopupMenu popupMenu = getPopupMenu(publisher, index);
                popupMenu.show(c, e.getX(), e.getY());
            }
            e.consume();
        });

        return publisher.hide();
    }

    @NotNull
    private static JPopupMenu getPopupMenu(PublishSubject<Integer> publisher, int index) {
        final JPopupMenu popupMenu = new JPopupMenu("git rebase -i");
        final JMenuItem menuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                publisher.onNext(index);
            }
        });
        menuItem.setText("start interactive rebase with this commit");
        popupMenu.add(menuItem);
        return popupMenu;
    }
}
