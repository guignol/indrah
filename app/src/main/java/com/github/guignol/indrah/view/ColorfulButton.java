package com.github.guignol.indrah.view;

import com.github.guignol.indrah.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

// https://stackoverflow.com/questions/14627223/how-to-change-a-jbutton-color-on-mouse-pressed
public class ColorfulButton extends JButton {

    public static AbstractButton create(@NotNull Colors.FlowColor baseColor) {
        return create(baseColor, null, null);
    }

    public static AbstractButton create(@NotNull Colors.FlowColor baseColor, @NotNull String text) {
        return create(baseColor, text, null);
    }

    public static AbstractButton create(@NotNull Colors.FlowColor baseColor, @Nullable String text, @Nullable Insets padding) {
        final ColorfulButton button = new ColorfulButton(baseColor.brighter(0.9), baseColor.darker(0.9));
        baseColor.background(button);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        if (text != null) {
            button.setText(text);
        }
        if (padding != null) {
            button.setColorfulMargin(padding);
        }
        return button;
    }

    @Nullable
    private Color hoveredColor;
    @Nullable
    private Color pressedColor;
    @Nullable
    private ColorfulBorder border;

    private ColorfulButton(@Nullable Colors.FlowColor hoveredColor, @Nullable Colors.FlowColor pressedColor) {
        super();
        if (pressedColor != null) {
            this.pressedColor = pressedColor.get();
            pressedColor.use(color -> {
                this.pressedColor = color;
                repaint();
            });
        }
        if (hoveredColor != null) {
            this.hoveredColor = hoveredColor.get();
            hoveredColor.use(color -> {
                this.hoveredColor = color;
                repaint();
            });
        }
        super.setContentAreaFilled(false);
    }

    @NotNull
    private Color getHoveredColor() {
        if (hoveredColor == null) {
            return getBackground().darker();
        } else {
            return hoveredColor;
        }
    }

    @NotNull
    private Color getPressedColor() {
        if (pressedColor == null) {
            return getBackground().brighter();
        } else {
            return pressedColor;
        }
    }

    @Override
    public void setContentAreaFilled(boolean b) {

    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isPressed()) {
            g.setColor(getPressedColor());
        } else if (getModel().isRollover()) {
            g.setColor(getHoveredColor());
        } else {
            g.setColor(getBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        if (border != null) {
            border.setColor(g.getColor());
        }

        super.paintComponent(g);
    }

    private void setColorfulMargin(Insets margin) {
        border = new ColorfulBorder(margin);
        setBorder(border);
    }

    private static class ColorfulBorder extends MatteBorder {

        ColorfulBorder(Insets insets) {
            super(insets, Color.BLACK);
        }

        void setColor(Color color) {
            this.color = color;
        }
    }
}
