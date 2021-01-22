package com.github.guignol.indrah.mvvm.dragdrop;

import com.github.guignol.indrah.model.IntHolder;
import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.functions.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Tracker implements IView<TrackerViewModel> {

    @Override
    public void bind(TrackerViewModel viewModel) {
        Bindable.view(doDrop.asObservable())
                .toViewModel(viewModel::doDrop);

        viewModel.onTracked().subscribe(this.trackOn());
        viewModel.onTrackEnd().subscribe(this.trackEnd());
    }

    private final Component root;
    private final Component target;
    private final Component targetMarker;
    private final Component trackMarker;
    private final Marker subMarker;
    private final Flash flash;

    private final EventStatus.Publisher doDrop = EventStatus.create();

    Tracker(@NotNull DragAndDropLayer layer, @NotNull Component target) {
        this.root = layer.getRoot();
        this.target = target;
        this.targetMarker = layer.targetMarker;
        this.trackMarker = layer.trackMarker;
        this.subMarker = layer.subMarker;
        this.flash = new Flash(this.trackMarker);
    }

    @NotNull
    private Rectangle getTargetRectangle() {
        return SwingUtilities.convertRectangle(target, target.getBounds(), root);
    }

    private CanDrop canDrop(Trackable original) {
        final Trackable atRoot = original.convert(root);
        final Rectangle dropTargetRect = getTargetRectangle();
        final boolean canDrop = containsHorizontally(dropTargetRect, atRoot.getMousePoint());
        return new CanDrop(canDrop, dropTargetRect, atRoot);
    }

    private Consumer<Trackable> trackOn() {
        final IntHolder startOffset = new IntHolder(-1);
        final IntHolder fixedHorizontally = new IntHolder(-1);
        return trackable -> {
            if (trackable.isValid()) {
                // trackMarker
                final int xOffsetWhenDragStarts = startOffset.getOrDefault(() -> trackable.getMousePoint().x);
                trackMarker.setVisible(true);
                final CanDrop canDrop = canDrop(trackable);
                final List<Point> optionalPoints = canDrop.atRoot.getOptionalPoints();
                final Rectangle centeredTracker = canDrop.atRoot.centerForMouse();
                centeredTracker.y = fixedHorizontally.getOrDefault(() -> canDrop.atRoot.getPosition().y);
                if (canDrop.yes) {
                    // trackMarker
                    centeredTracker.width = canDrop.target.width;
                    centeredTracker.x = canDrop.target.x;
                    trackMarker.setBounds(centeredTracker);

                    // subMarkers
                    final List<JLabel> subMarkers = showSubMarkers(optionalPoints, centeredTracker);

                    // targetMarker
                    targetMarker.setBounds(canDrop.target);
                    targetMarker.setVisible(true);
                    // flash
                    flash.startWith(subMarkers);
                } else {
                    // trackMarker
                    if (xOffsetWhenDragStarts < canDrop.target.x) {
                        // ドラッグ開始位置が左側
                        centeredTracker.x = centeredTracker.width;
                        centeredTracker.width = canDrop.atRoot.getMousePoint().x - xOffsetWhenDragStarts;
                    } else {
                        // ドラッグ開始位置が右側
                        centeredTracker.x = canDrop.atRoot.getMousePoint().x - xOffsetWhenDragStarts;
                        centeredTracker.width = root.getWidth() - centeredTracker.width - centeredTracker.x;
                    }
                    trackMarker.setBounds(centeredTracker);

                    // subMarkers
                    showSubMarkers(optionalPoints, centeredTracker);

                    // targetMarker
                    targetMarker.setVisible(false);
                    // flash
                    flash.stopGracefully();
                }
            } else {
                // targetMarker
                targetMarker.setVisible(false);
                // flash
                flash.stopGracefully(false);

                startOffset.reset();
                fixedHorizontally.reset();
            }
        };
    }

    private List<JLabel> showSubMarkers(List<Point> optionalPoints, Rectangle centeredTracker) {
        final List<JLabel> subMarkers = subMarker.get(optionalPoints.size());
        ListUtils.forEach(optionalPoints, (point, index) -> {
            final JLabel label = subMarkers.get(index);
            centeredTracker.y = point.y;
            label.setBounds(centeredTracker);
            label.setVisible(true);
        });
        return subMarkers;
    }

    private Consumer<Trackable> trackEnd() {
        return trackable -> {
            if (trackable.isValid()) {
                trackEnd(canDrop(trackable).yes);
            }
        };
    }

    private void trackEnd(boolean drop) {
        if (drop) {
            doDrop.onNext();

            // flashで余韻
            flash.auto(false, 300);
        } else {
            subMarker.hide();
        }
    }

    private static boolean containsHorizontally(Rectangle bounds, Point point) {
        return bounds.x <= point.x && point.x <= bounds.x + bounds.width;
    }

}
