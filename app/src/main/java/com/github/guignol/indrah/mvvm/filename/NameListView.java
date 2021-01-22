package com.github.guignol.indrah.mvvm.filename;

import com.github.guignol.indrah.model.IntHolder;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.model.swing.LockableSelectionModel;
import com.github.guignol.indrah.model.swing.MousePress;
import com.github.guignol.indrah.model.swing.MousePressDetector;
import com.github.guignol.indrah.mvvm.dragdrop.Trackable;
import com.github.guignol.indrah.view.list.UpperListContainer;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.binding.Keys;
import com.github.guignol.swing.processor.View;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

@View
public class NameListView implements ComponentHolder, IView<NameListViewModel> {

    @Override
    public void bind(NameListViewModel viewModel) {

        // to ViewModel
        Bindable.view(list)
                .toViewModel(viewModel::select);
        Bindable.view(exportSelection.asObservable())
                .toViewModel(viewModel::exportSelection);
        Bindable.view(trackOn.hide())
                .toViewModel(viewModel::trackOnDrag);
        Bindable.view(trackEnd.hide())
                .toViewModel(viewModel::trackOnDrop);
        Bindable.view(list, Keys.COPY)
                .toViewModel(viewModel::toClipboard);

        // from ViewModel
        list.setModel(viewModel.dataModel);
        viewModel.onDataUpdated().subscribe(status -> {
            System.out.println(toString() + ": onDataUpdated: ");
            list.updateUI();

            listContainer.alignBottom();
        });
        // 外部から選択の変更を受ける場合
        viewModel.onSelectionImported().subscribe(indices -> {
            System.out.println(toString() + ": onSelectionImported: ");
            if (indices.length == 0) {
                list.clearSelection();
            } else {
                list.setSelectedIndices(indices);
            }
        });
        viewModel.onFocusRequested().subscribe(status -> list.requestFocus());
    }

    private final UpperListContainer<String> listContainer = new UpperListContainer<>(50);
    private final JList<String> list = listContainer.list;

    // to ViewModel
    private final EventStatus.Publisher exportSelection = EventStatus.create();
    private final PublishSubject<Trackable> trackOn = PublishSubject.create();
    private final PublishSubject<Trackable> trackEnd = PublishSubject.create();

    NameListView() {
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                final JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                component.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return component;
            }
        });

        final LockableSelectionModel selectionModel = new LockableSelectionModel()
                .onUnlocked(() -> trackOn.onNext(Trackable.NONE))
                .onLocked(() -> {
                    System.out.println(toString() + ": onLocked: ");
                    exportSelection();
                });
        list.setSelectionModel(selectionModel);

        final IntHolder maybeDrag = new IntHolder(-1);
        final MousePressDetector mousePressDetector = new MousePressDetector() {

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                final int pressed = list.locationToIndex(mouseEvent.getPoint());
                if (list.isSelectedIndex(pressed)) {
                    // 選択中のセルの上でマウスプレスした場合はドラッグの可能性がある
                    maybeDrag.set(pressed);
                    selectionModel.lock();
                    trackOn.onNext(Trackable.NONE);
                } else {
                    maybeDrag.set(-1);
                    selectionModel.unlock();
                }
                super.mousePressed(mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (selectionModel.isLocked()) {
                    trackEnd.onNext(Trackable.from(mouseEvent, 0, 0));
                    // 直後にListSelectionListenerが呼ばれて選択が解除されるため、フォーカスを外す
                    listContainer.scrollPane.requestFocus();
                }

                final int pressed = maybeDrag.getOrDefault(() -> -1);
                final boolean unDragged = -1 < pressed && mousePress.getEvent() != null;

                super.mouseReleased(mouseEvent);

                selectionModel.unlock();
                if (unDragged) {
                    // ドラッグしなかった場合は、クリックしたセルの選択を解除する
                    list.removeSelectionInterval(pressed, pressed);
                }
            }
        };
        list.addMouseListener(mousePressDetector);
        final MousePress mousePress = mousePressDetector.mousePress;
        list.addListSelectionListener(e -> {
            // UIからの選択以外では無視する
            if (!list.hasFocus()) {
                return;
            }

            final boolean fire;
            if (mousePress.getEvent() == null) {
                // 方向キーの選択はadjustingにならない
                fire = !e.getValueIsAdjusting();
            } else {
                // ドラッグ中も表示を変えたいのでadjustingのときに
                fire = e.getValueIsAdjusting();
            }
            System.out.println(toString() + ": ListSelectionListener: ");
            if (fire) {
                exportSelection();
            }
        });
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 縦ドラッグによる選択をさせない
                selectionModel.lock();

                final Trackable trackable;
                // 複数選択の場合は横ドラッグしない
                if (selectionModel.isLocked()) {
                    // ドラッグ開始セルを固定
                    final int selectedIndex = maybeDrag.getOrDefault(() -> list.locationToIndex(e.getPoint()));
                    // セルの位置とサイズを伝える
                    final Rectangle cellBounds = list.getCellBounds(selectedIndex, selectedIndex);
                    // リストの幅は内部ラベルの下限（つまり文字数）に依存してしまうため、scrollPaneの幅を使う
                    cellBounds.width = listContainer.scrollPane.getWidth();
                    trackable = Trackable.from(e, cellBounds, selectedCellPoints(selectedIndex));
                } else {
                    trackable = Trackable.NONE;
                }
                trackOn.onNext(trackable);
                // ドラッグした目印
                mousePress.setEvent(null);
            }
        });
    }

    private List<Point> selectedCellPoints(int exceptional) {
        final List<Point> points = new ArrayList<>();
        for (int i : list.getSelectedIndices()) {
            if (i == exceptional) {
                continue;
            }
            final Rectangle cellBounds = list.getCellBounds(i, i);
            points.add(cellBounds.getLocation());
        }
        return points;
    }

    private void exportSelection() {
        System.out.println(toString() + ": exportSelection: ");
        exportSelection.onNext();
    }

//    public void showListWithSelectionOnInit(List<Diff> data) {
//        showList(data);
//        // TODO 初期選択
//        if (!onSelected.hasValue()) {
//            list.setSelectedIndex(dataModel.getSize() - 1);
//            exportSelection();
//        }
//    }

    @Override
    public Component getComponent() {
        return listContainer.getComponent();
    }
}
