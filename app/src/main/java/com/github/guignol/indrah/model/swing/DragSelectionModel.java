package com.github.guignol.indrah.model.swing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DragSelectionModel extends DefaultListSelectionModel {

    private final Drag<Integer> drag;
    private final Validator validator;
    private final Dispatcher dispatcher;

    public DragSelectionModel(@NotNull Drag<Integer> drag,
                              @Nullable Validator validator,
                              @Nullable Dispatcher dispatcher) {
        this.drag = drag;
        if (validator == null) {
            validator = (from, to) -> true;
        }
        this.validator = validator;
        if (dispatcher == null) {
            dispatcher = (selection) -> false;
        }
        this.dispatcher = dispatcher;

        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }

    @Override
    public void setSelectionInterval(int fromIndex, final int toIndex) {
//        System.out.println(fromIndex + " , " + toIndex);
//        System.out.println("drag start from: " + drag.from());

        // JListの場合は以下、JTableの場合はsetSelectionInterval（ここ）がmousePressedより先に呼ばれる（のでinvokeLaterしている）

        //【ドラッグの場合の呼び出し順序】
        // mousePressed             (prepared: false)
        // setSelectionInterval     (prepared: false ⇒ true)
        //  setSelectionInterval    (prepared: true)
        //  setSelectionInterval    (prepared: true)
        //  setSelectionInterval    (prepared: true)
        // mouseReleased            (prepared: true ⇒ false)

        //【クリックの場合の呼び出し順序】
        // mousePressed             (prepared: false)
        // setSelectionInterval     (prepared: false ⇒ true)
        // mouseReleased            (prepared: true ⇒ false)
        // mouseClicked             (prepared: false)

        if (fromIndex == toIndex
                && dispatcher.dispatchSingleSelection(fromIndex)) {
            return;
        }

        if (drag.isDragging()) {
            fromIndex = drag.from();
        }
        if (validator.isValidSelection(fromIndex, toIndex)) {
            super.setSelectionInterval(fromIndex, toIndex);
            drag.prepare(fromIndex);
        } else {
            clearSelection();
        }
    }

    public interface Validator {
        boolean isValidSelection(int from, int to);
    }

    public interface Dispatcher {
        boolean dispatchSingleSelection(int clickedIndex);
    }
}
