package com.github.guignol.indrah.mvvm.common;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

public class Popup {

    public static class Builder {

        private final Component content;
        private Component target;

        private Builder(@NotNull final Component content) {
            this.content = content;
        }

        public Builder onto(@NotNull final Component target) {
            this.target = target;
            return this;
        }

        public Popup forSize(@NotNull final Supplier<Dimension> size) {
            return new Popup(content, size, target);
        }
    }

    public static Builder with(@NotNull final Component content) {
        return new Builder(content);
    }

    // TODO ダイアログだと表示時にステータスバーが変わるのでダサい
    private JDialog dialog;
    private final Timer hideTimer;
    private final Timer showTimer;
    private final Supplier<Boolean> prepare;

    private Popup(@NotNull final Component content,
                  @NotNull final Supplier<Dimension> sizer,
                  @NotNull final Component target) {

        showTimer = new Timer(200, null);
        showTimer.addActionListener(e -> {
            if (dialog != null) {
                dialog.setVisible(true);
            }
            showTimer.stop();
        });

        hideTimer = new Timer(300, null);
        hideTimer.addActionListener(e -> {
            if (dialog != null) {
                dialog.setVisible(false);
            }
            hideTimer.stop();
        });

        prepare = () -> {
            final JFrame frame = (JFrame) SwingUtilities.windowForComponent(target);
            if (dialog == null) {
                dialog = new JDialog(frame);
                // MODELESSにしないとmouseExitedが呼ばれない
                dialog.setModalityType(JDialog.ModalityType.MODELESS);
                dialog.setUndecorated(true);
                dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
                dialog.getContentPane().add(content, BorderLayout.CENTER);
            }
            final Dimension size = sizer.get();
            if (size.width == 0 || size.height == 0) {
                dialog.setVisible(false);
                return false;
            }
            dialog.setSize(size);
            final Point onScreen = target.getLocationOnScreen();
            onScreen.translate(dialog.getWidth() * -1, 0);
            dialog.setLocation(onScreen);
            return true;
        };

        target.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (prepare.get()) {
                    show();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hide();
            }
        });
    }

    public void reshowIfShown() {
        if (dialog != null && dialog.isVisible()) {
            prepare.get();
        }
    }

    public void show() {
        hideTimer.stop();
        showTimer.start();
    }

    public void hide() {
        showTimer.stop();
        hideTimer.start();
    }

    public void hideNow() {
        showTimer.stop();
        hideTimer.stop();
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

    public void doNotHideOn(@NotNull final Component... components) {
        for (Component component : components) {
            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hideTimer.stop();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hide();
                }
            });
        }
    }
}
