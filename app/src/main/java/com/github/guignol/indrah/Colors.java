package com.github.guignol.indrah;

import com.github.guignol.indrah.model.ColorPair;
import com.github.guignol.indrah.mvvm.ValueHolder;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import java.awt.*;
import java.util.function.Function;

public class Colors {

    public static class FlowColor {

        private final ValueHolder<Color> color = ValueHolder.create();

        public Disposable use(Consumer<Color> consumer) {
            return color.observable().subscribe(consumer);
        }

        public Color get() {
            return color.get();
        }

        public Disposable foreground(Component component) {
            return use(component::setForeground);
        }

        public Disposable background(Component component) {
            return use(component::setBackground);
        }

        public FlowColor cascade(Function<Color, Color> converter) {
            final FlowColor value = new FlowColor();
            this.use(t -> value.put(converter.apply(t)));
            return value;
        }

        public FlowColor brighter(double FACTOR) {
            return cascade(before -> Colors.brighter(before, FACTOR));
        }

        public FlowColor darker(double FACTOR) {
            return cascade(before -> Colors.darker(before, FACTOR));
        }

        private FlowColor put(Color color) {
            this.color.put(color);
            return this;
        }
    }

    public static final FlowColor PRIME = new FlowColor().put(new Color(19, 122, 199));
    public static final FlowColor HEAVY = PRIME.darker(0.6);
    public static final FlowColor LITE = PRIME.brighter(0.95);
    public static final FlowColor POPUP_BACK = Colors.LITE;
    public static final FlowColor POPUP_BUTTON = Colors.HEAVY.brighter(0.7);
    public static final FlowColor SEPARATOR = PRIME.darker(0.9);
    public static final FlowColor SEPARATOR_BRIGHT = PRIME;
    public static final FlowColor SELECTED_FILE_NAME = new FlowColor().put(new Color(184, 207, 229));
    public static final FlowColor SELECTED_LITE = SELECTED_FILE_NAME.cascade(color -> setAlpha(color, 100));
    public static final FlowColor DARK_THEME = new FlowColor().put(new Color(64, 64, 64));

    public static final Diff DIFF = new Diff(DARK_THEME);

    public static void change(Color prime) {
        PRIME.put(prime);
    }

    public static class Diff {
        public final FlowColor HEADER_BACK;
        public final ColorPair TEXT;
        public final ColorPair BACK;
        public final Color SPECIAL;

        public Diff(FlowColor darkTheme) {
            HEADER_BACK = darkTheme;
            TEXT = new ColorPair(new Color(0, 155, 0), new Color(255, 0, 0));
            BACK = new ColorPair(new Color(99, 202, 99), new Color(255, 175, 175));
            SPECIAL = new Color(19, 122, 199);
        }
    }

    public static Color setAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // Color#brighter FACTORが0.7 */
    public static Color brighter(Color color, double FACTOR) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255),
                alpha);
    }

    public static Color darker(Color color, double FACTOR) {
        return new Color(Math.max((int) (color.getRed() * FACTOR), 0),
                Math.max((int) (color.getGreen() * FACTOR), 0),
                Math.max((int) (color.getBlue() * FACTOR), 0),
                color.getAlpha());
    }
}
