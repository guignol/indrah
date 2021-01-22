package com.github.guignol.indrah.model.swing;

import com.github.guignol.indrah.utils.Functionals;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;

public class MousePress {

    public static MousePress detect(Component component) {
        final MousePressDetector mousePressDetector = new MousePressDetector(Functionals.doNothing);
        component.addMouseListener(mousePressDetector);
        return mousePressDetector.mousePress;
    }

    private MouseEvent event = null;

    public void setEvent(@Nullable MouseEvent event) {
        this.event = event;
    }

    @Nullable
    public MouseEvent getEvent() {
        return event;
    }
}
