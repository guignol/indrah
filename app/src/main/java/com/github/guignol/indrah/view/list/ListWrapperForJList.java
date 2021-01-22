package com.github.guignol.indrah.view.list;

import com.github.guignol.indrah.model.swing.ListItem;
import com.github.guignol.indrah.model.swing.SimpleListModel;
import com.github.guignol.swing.binding.Keys;
import com.github.guignol.swing.binding.Property;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.MouseListener;

public class ListWrapperForJList<T extends ListItem> implements ListWrapper {
    private final JList<T> list;
    private final JScrollPane scrollPane;

    public ListWrapperForJList(SimpleListModel<T> dataModel,
                               ListRenderer<ListItem> renderer) {
        this.list = new JList<>();
        this.list.setCellRenderer(new DiffAdapter<>(renderer));
        this.list.setModel(dataModel);
        scrollPane = new JScrollPane();
        scrollPane.getViewport().setView(this.list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        list.addMouseListener(listener);
    }

    @Override
    public void setSelectionModel(ListSelectionModel selectionModel) {
        list.setSelectionModel(selectionModel);
    }

    ////////////////////////////////////////

    @Override
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    @Override
    public void updateUI() {
        list.updateUI();
    }

    @Override
    public Observable<int[]> selectedIndices() {
        return Property.onSelection(list);
    }

    @Override
    public Keys.Registry getKeysRegistry() {
        return new Keys.Registry(this.list);
    }
}
