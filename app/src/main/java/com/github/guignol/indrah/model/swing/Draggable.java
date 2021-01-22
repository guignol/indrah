package com.github.guignol.indrah.model.swing;

import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Draggable {

    public static Disposable vertically(Component component) {
        final Draggable draggable = new Draggable(component);
        return draggable.asObservable()
                .subscribe(point -> component.setLocation(component.getLocation().x, point.y));
    }

    public static Disposable horizontally(Component component) {
        final Draggable draggable = new Draggable(component);
        return draggable.asObservable()
                .subscribe(point -> component.setLocation(point.x, component.getLocation().y));
    }

    public static Disposable with(Component component) {
        final Draggable draggable = new Draggable(component);
        return draggable.asObservable()
                .subscribe(component::setLocation);
    }

    private final PublishSubject<Point> pointSubject = PublishSubject.create();

    private final EventStatus.Publisher onRelease = EventStatus.create();

    public Draggable(Component component) {

        final MousePress mousePress = MousePress.detect(component);
        component.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                final MouseEvent event = mousePress.getEvent();
                if (event == null) {
                    return;
                }
                final Point start = event.getPoint();
                final Point converted = SwingUtilities.convertPoint(component, e.getPoint(), component.getParent());
                converted.translate(start.x * -1, start.y * -1);
                pointSubject.onNext(converted);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                onRelease.onNext();
            }
        });
    }

    // 移動量を加算した位置
    public Observable<Point> asObservable() {
        return pointSubject.hide();
    }

    public Observable<EventStatus> onRelease() {
        return onRelease.asObservable();
    }
}
