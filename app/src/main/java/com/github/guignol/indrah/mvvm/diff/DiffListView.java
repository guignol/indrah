package com.github.guignol.indrah.mvvm.diff;

import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.view.list.ListWrapper;
import com.github.guignol.indrah.view.list.ListWrapperForJList;
import com.github.guignol.indrah.view.list.ListWrapperForJTable;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.binding.Keys;
import com.github.guignol.swing.processor.View;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@View
public class DiffListView implements IView<DiffListViewModel>, ComponentHolder {

    @Override
    public void bind(DiffListViewModel viewModel) {
        // create list
        if (DiffListViewModel.table) {
            list = new ListWrapperForJTable(viewModel.listModel, new DiffRenderer<>());
        } else {
            list = new ListWrapperForJList<>(viewModel.listModel, new DiffRenderer<>());
        }
        list.setSelectionModel(viewModel.selectionModel);
        list.addMouseListener(viewModel.mouseListener);

        // to ViewModel
        final Keys.Registry key = list.getKeysRegistry();
        Bindable.view(key.onFired(Keys.COPY))
                .toViewModel(viewModel::toClipboard);
        Bindable.view(key.onFired(Keys.SELECT_ALL))
                .toViewModel(viewModel::selectAll);
        if (viewModel.canTrash()) {
            Bindable.view(undo(key))
                    .toViewModel(viewModel::undo);
        }
        Bindable.view(list.selectedIndices())
                .toViewModel(viewModel::select);

        // from ViewModel
        viewModel.onUpdate().subscribe(diffs -> list.updateUI());
    }

    private ListWrapper list;

    @Override
    public Component getComponent() {
        return list.getScrollPane();
    }

    private static Observable<EventStatus> undo(Keys.Registry keysRegistry) {

        final EventStatus.Publisher publisher = EventStatus.create();
        // TODO 右クリックメニュー以外のUIを考える
        final JPopupMenu popupMenu = new JPopupMenu("ポップアップメニューのテスト");
//        popupMenu.add("メニュー1");
//        popupMenu.add("メニュー2");
//        popupMenu.add("メニュー3");
        final JMenuItem menuItem = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                publisher.onNext();
            }
        });
        menuItem.setText("破棄する");
        popupMenu.add(menuItem);
        // TODO 選択エリアの上でのクリックかどうか（UIが確定してから）
        keysRegistry.onRightClick().subscribe(e -> {
            // ポップアップメニューを表示する
            final JComponent c = (JComponent) e.getSource();
            popupMenu.show(c, e.getX(), e.getY());
            e.consume();
        });

        return publisher.asObservable();
    }
}
