package com.github.guignol.indrah.mvvm.sidebar;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.view.ColorfulButton;

import javax.swing.*;
import java.awt.*;

public class SideBarButtons {

    private final static Insets padding = new Insets(0, 10, 0, 10);

    public static AbstractButton init(String text) {
        final AbstractButton button = ColorfulButton.create(Colors.PRIME, text, padding);
        initButton(button);
        return button;
    }

    private static void initButton(AbstractButton button) {
        button.setForeground(Color.WHITE);
        Colors.PRIME.background(button);
        // 右寄せ
//        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
//        button.setHorizontalAlignment(SwingConstants.TRAILING);
        // 左寄せ
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEADING);
    }

    public static JPanel getPanel(int height) {
        final JPanel panel = new JPanel();
        panel.setOpaque(false);

        panel.setLayout(new GridLayout(0, 1));
        final Dimension size = new Dimension(Short.MAX_VALUE, height);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);
        panel.setMinimumSize(size);

        return panel;
    }
}
