package com.github.guignol.indrah.model.swing;

import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MousePressReactor implements MouseListener {

    private final Runnable reset;

    public MousePressReactor(@NotNull Runnable reset) {
        this.reset = reset;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
//        System.out.println("  mouseClicked");
//        delegate.reset(); // mouseClickedの前にmouseReleasedが呼ばれるので不要
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
//        System.out.println("mousePressed");
        reset.run();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
//        System.out.println(" mouseReleased");
        reset.run();
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
//        System.out.println("mouseEntered");
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
//        System.out.println("mouseExited");
    }
}
