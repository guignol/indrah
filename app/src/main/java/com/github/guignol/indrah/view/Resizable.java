package com.github.guignol.indrah.view;

import com.github.guignol.indrah.model.Edge;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import java.awt.*;

public class Resizable {

    public static ResizableBox layoutHorizontally(final int width) {
        return layoutHorizontally(new JPanel(), new JPanel(), new JPanel(), width);
    }

    public static ResizableBox layoutHorizontally(final Component navigatorComponent) {
        return layoutHorizontally(new JPanel(), new JPanel(), new JPanel(), navigatorComponent);
    }

    public static ResizableBox layoutHorizontally(final Container parent,
                                                  final Container leftComponent,
                                                  final Container rightComponent,
                                                  final Component navigatorComponent) {
        final PublishSubject<Edge> edgePublisher = PublishSubject.create();
        final Navigator navigator = Navigator.horizontal(navigatorComponent,
                (left, right) -> {
                    final Edge edge = ResizableAction.onDragHorizontally(parent, leftComponent, rightComponent, left, right);
                    edgePublisher.onNext(edge);
                });
        return layoutHorizontally(parent, leftComponent, rightComponent, navigator, edgePublisher.hide());
    }

    public static ResizableBox layoutHorizontally(final Container parent,
                                                  final Container leftComponent,
                                                  final Container rightComponent,
                                                  final int width) {
        final PublishSubject<Edge> edgePublisher = PublishSubject.create();
        final Navigator navigator = Navigator.horizontal(width,
                (left, right) -> {
                    final Edge edge = ResizableAction.onDragHorizontally(parent, leftComponent, rightComponent, left, right);
                    edgePublisher.onNext(edge);
                });
        return layoutHorizontally(parent, leftComponent, rightComponent, navigator, edgePublisher.hide());
    }

    private static ResizableBox layoutHorizontally(final Container parent,
                                                   final Container leftComponent,
                                                   final Container rightComponent,
                                                   final Navigator navigator,
                                                   final Observable<Edge> edgePublisher) {
        parent.setLayout(new BoxLayout(parent, BoxLayout.X_AXIS));
        parent.add(leftComponent);
        parent.add(navigator.getComponent());
        parent.add(rightComponent);

        // setPreferredSizeしないと、限界まで広がらない
        leftComponent.setPreferredSize(new Dimension(500, Short.MAX_VALUE));
        rightComponent.setPreferredSize(new Dimension(500, Short.MAX_VALUE));

        return new ResizableBox(parent, leftComponent, rightComponent, navigator, edgePublisher);
    }

    public static ResizableBox layoutVertically(final Container parent,
                                                final Container topComponent,
                                                final Container bottomComponent,
                                                final Component navigatorComponent) {
        final PublishSubject<Edge> edgePublisher = PublishSubject.create();
        final Navigator navigator = Navigator.vertical(navigatorComponent,
                (top, bottom) -> {
                    final Edge edge = ResizableAction.onDragVertically(parent, topComponent, bottomComponent, top, bottom);
                    edgePublisher.onNext(edge);
                });
        return layoutVertically(parent, topComponent, bottomComponent, navigator, edgePublisher.hide());
    }

    public static ResizableBox layoutVertically(final Container parent,
                                                final Container topComponent,
                                                final Container bottomComponent,
                                                final int height) {
        final PublishSubject<Edge> edgePublisher = PublishSubject.create();
        final Navigator navigator = Navigator.vertical(height,
                (top, bottom) -> {
                    final Edge edge = ResizableAction.onDragVertically(parent, topComponent, bottomComponent, top, bottom);
                    edgePublisher.onNext(edge);
                });
        return layoutVertically(parent, topComponent, bottomComponent, navigator, edgePublisher.hide());
    }

    private static ResizableBox layoutVertically(final Container parent,
                                                 final Container topComponent,
                                                 final Container bottomComponent,
                                                 final Navigator navigator,
                                                 final Observable<Edge> edgePublisher) {
        parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
        parent.add(topComponent);
        parent.add(navigator.getComponent());
        parent.add(bottomComponent);

        // setPreferredSizeしないと、限界まで広がらない
        topComponent.setPreferredSize(new Dimension(Short.MAX_VALUE, 500));
        bottomComponent.setPreferredSize(new Dimension(Short.MAX_VALUE, 500));

        return new ResizableBox(parent, topComponent, bottomComponent, navigator, edgePublisher);
    }
}
