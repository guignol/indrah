package com.github.guignol.indrah.mvvm.dragdrop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Trackable {

    @Nullable
    private final MouseEvent event;
    private final Rectangle bounds;
    private final List<Point> optionalPoints = new ArrayList<>();

    private Trackable(@Nullable MouseEvent event, Point position, @NotNull Dimension size) {
        this.event = event;
        this.bounds = new Rectangle(position, size);
    }

    private Trackable withPoints(List<Point> points) {
        optionalPoints.clear();
        optionalPoints.addAll(points);
        return this;
    }

    public List<Point> getOptionalPoints() {
        return new ArrayList<>(optionalPoints);
    }

    public Point getPosition() {
        return bounds.getLocation();
    }

    public Point getMousePoint() {
        if (event == null) {
            return new Point();
        }
        return event.getPoint();
    }

    public boolean isValid() {
        return event != null;
    }

    public Trackable convert(Component target) {
        if (event == null) {
            return NONE;
        } else {
            final Component source = (Component) event.getSource();
            final MouseEvent convertedEvent = SwingUtilities.convertMouseEvent(source, event, target);
            final Point convertedPoint = SwingUtilities.convertPoint(source, bounds.getLocation(), target);
            return Trackable.from(convertedEvent, new Rectangle(convertedPoint, bounds.getSize()))
                    .withPoints(optionalPoints.stream()
                            .map(point -> SwingUtilities.convertPoint(source, point, target))
                            .collect(Collectors.toList()));
        }
    }

    public Rectangle centerForMouse() {
        if (event == null) {
            return new Rectangle();
        }
        final Point point;
        point = event.getPoint();
        point.x -= bounds.getSize().width / 2;
        point.y -= bounds.getSize().height / 2;
        return new Rectangle(point, bounds.getSize());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trackable trackable = (Trackable) o;

        if (event != null ? !event.equals(trackable.event) : trackable.event != null) return false;
        if (!bounds.equals(trackable.bounds)) return false;
        return optionalPoints.equals(trackable.optionalPoints);
    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + bounds.hashCode();
        return result;
    }

    public static Trackable from(MouseEvent event, int width, int height) {
        return Trackable.from(event, 0, 0, width, height);
    }

    private static Trackable from(MouseEvent event, int x, int y, int width, int height) {
        return new Trackable(event, new Point(x, y), new Dimension(width, height));
    }

    private static Trackable from(MouseEvent event, Rectangle bounds) {
        return new Trackable(event, bounds.getLocation(), bounds.getSize());
    }

    public static Trackable from(MouseEvent event, Rectangle bounds, List<Point> points) {
        return new Trackable(event, bounds.getLocation(), bounds.getSize()).withPoints(points);
    }

    public static final Trackable NONE = Trackable.from(null, 0, 0, 0, 0);
}
