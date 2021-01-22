package com.github.guignol.indrah.model.swing;

import com.github.guignol.indrah.utils.Functionals;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MousePressDetector extends MousePressReactor implements MouseListener {

    @NotNull
    public final MousePress mousePress = new MousePress();

    public MousePressDetector() {
        super(Functionals.doNothing);
    }

    public MousePressDetector(@NotNull Runnable dragReset) {
        super(dragReset);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);
        mousePress.setEvent(mouseEvent);
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        mousePress.setEvent(null);
    }
}
