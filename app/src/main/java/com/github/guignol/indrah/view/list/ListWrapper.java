package com.github.guignol.indrah.view.list;

import com.github.guignol.swing.binding.Keys;
import io.reactivex.Observable;

import javax.swing.*;
import java.awt.event.MouseListener;

public interface ListWrapper {

    void addMouseListener(MouseListener listener);

    void setSelectionModel(ListSelectionModel selectionModel);

    ////////////////////////////////////////

    JScrollPane getScrollPane();

    void updateUI();

    Observable<int[]> selectedIndices();

    Keys.Registry getKeysRegistry();
}
