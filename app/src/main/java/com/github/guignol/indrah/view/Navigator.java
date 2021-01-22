package com.github.guignol.indrah.view;

import com.github.guignol.indrah.Colors;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.rx.Animation;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public abstract class Navigator implements ComponentHolder {

    public static Navigator horizontal(final int width, final BiConsumer<Integer, Integer> onDrag) {
        return Navigator.horizontal(defaultComponent(true, width), onDrag);
    }

    public static Navigator horizontal(final Component component, final BiConsumer<Integer, Integer> onDrag) {
        return new Navigator(component, onDrag) {
            @Override
            int getPosition(Point point) {
                return point.x;
            }

            @Override
            int getSize(Component component) {
                return component.getWidth();
            }
        };
    }

    public static Navigator vertical(final int height, final BiConsumer<Integer, Integer> onDrag) {
        return Navigator.vertical(defaultComponent(false, height), onDrag);
    }

    public static Navigator vertical(final Component component, final BiConsumer<Integer, Integer> onDrag) {
        return new Navigator(component, onDrag) {
            @Override
            int getPosition(Point point) {
                return point.y;
            }

            @Override
            int getSize(Component component) {
                return component.getHeight();
            }
        };
    }

    private static Component defaultComponent(boolean vertical, int size) {
        JComponent component = new JSeparator(vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL);
        component.setOpaque(true);
        init(component, vertical, size);
        return component;
    }

    public static Box initAsBox(boolean vertical, int size) {
        if (vertical) {
            return init(Box.createVerticalBox(), true, size);
        } else {
            return init(Box.createHorizontalBox(), false, size);
        }
    }

    public static <T extends Component> T init(T component, boolean vertical, int size) {
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(true);
        }

        Colors.SEPARATOR.foreground(component);
        Colors.SEPARATOR.background(component);
        final Dimension dimension = size(vertical, size, Short.MAX_VALUE);
        component.setMaximumSize(dimension);
        component.setPreferredSize(dimension);
        component.setMinimumSize(size(vertical, size, 0));
        return component;
    }

    public static Dimension size(boolean vertical, int size, int limit) {
        if (vertical) {
            return new Dimension(size, limit);
        } else {
            return new Dimension(limit, size);
        }
    }

    private final Component component;
    private final BiConsumer<Integer, Integer> onDrag;
    private BiConsumer<Integer, Integer> dispatcher = null;

    public boolean dispatchToParent = false;

    private Navigator(final Component component, final BiConsumer<Integer, Integer> onDrag) {
        this.component = component;
        this.onDrag = onDrag;
        final MouseInputAdapter mouseAdapter = new MouseInputAdapter() {

            private int dragStart;

            @Override
            public void mousePressed(MouseEvent e) {
                // ドラッグ開始位置で調整する
                dragStart = getPosition(e.getPoint());
                if (dispatchToParent) {
                    final Component source = e.getComponent();
                    final Container parent = source.getParent();
                    final MouseEvent converted = SwingUtilities.convertMouseEvent(source, e, parent);
                    parent.dispatchEvent(converted);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                final Component source = e.getComponent();
                final Container parent = source.getParent();
                final MouseEvent converted = SwingUtilities.convertMouseEvent(source, e, parent);
                moveHead(getPosition(converted.getPoint()) - dragStart);
                if (dispatchToParent) {
                    parent.dispatchEvent(converted);
                }
            }
        };
        component.addMouseListener(mouseAdapter);
        component.addMouseMotionListener(mouseAdapter);
    }

    abstract int getPosition(Point point);

    abstract int getSize(Component component);

    private int getPosition() {
        return getPosition(new Point(component.getX(), component.getY()));
    }

    private int getSize() {
        return getSize(component);
    }

    private void moveHead(int headPosition) {
        move(headPosition, headPosition + getSize());
    }

    private void moveTail(int tailPosition) {
        move(tailPosition - getSize(), tailPosition);
    }

    public void moveHeadSlowly(int headPosition) {
        final int start = getPosition();
        Animation.slowly(start, headPosition).subscribe(this::moveHead);
    }

    public void moveTailSlowly(int tailPosition) {
        moveHeadSlowly(tailPosition - getSize());
    }

    private void move(int headPosition, int tailPosition) {
        if (dispatcher != null) {
            dispatcher.accept(headPosition, tailPosition);
        }
        this.onDrag.accept(headPosition, tailPosition);
    }

    @Override
    public Component getComponent() {
        return component;
    }

    public void setDispatcher(BiConsumer<Integer, Integer> dispatcher) {
        if (this.dispatcher != null) {
            throw new RuntimeException();
        }
        this.dispatcher = dispatcher;
    }
}
