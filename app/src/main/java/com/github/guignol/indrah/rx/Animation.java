package com.github.guignol.indrah.rx;

import com.github.guignol.indrah.model.Direction;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Animation {

    public static Disposable slideIn(final Direction from, final Component component) {
        component.setVisible(false);
        final Point goal = new Point();
        final int width = component.getWidth();
        final int height = component.getHeight();
        final int parentHeight = component.getParent().getHeight();
        goal.y = parentHeight - height;
        switch (from) {
            case LEFT:
                goal.x = 0;
                component.setLocation(0 - width, goal.y);
                break;
            case RIGHT:
                final int parentWidth = component.getParent().getWidth();
                goal.x = parentWidth - width;
                component.setLocation(parentWidth, goal.y);
                break;
            case TOP:
                goal.y = 0;
                component.setLocation(0, 0 - height);
                break;
            case BOTTOM:
                component.setLocation(0, parentHeight);
                break;
        }
        component.setVisible(true);
        final Rectangle bounds = new Rectangle(goal, component.getSize());
        return resize(component.getBounds(), bounds)
                .subscribe(component::setBounds);
    }

    public static Disposable resizeWidth(final Component component, final int width) {
        return slowly(component.getWidth(), width).subscribe(value -> component.setSize(value, component.getHeight()));
    }

    public static Observable<Rectangle> resize(final Rectangle original, final Rectangle bounds) {
        return resize(original, bounds, 500);
    }

    public static Observable<Rectangle> resize(final Rectangle original, final Rectangle bounds, final int durationMilliSeconds) {
        return Observable.zip(
                Animation.slowly(original.x, bounds.x, durationMilliSeconds),
                Animation.slowly(original.y, bounds.y, durationMilliSeconds),
                Animation.slowly(original.width, bounds.width, durationMilliSeconds),
                Animation.slowly(original.height, bounds.height, durationMilliSeconds),
                Rectangle::new);
    }

    public static Observable<Integer> slowly(final int start, final int goal) {
        return slowly(start, goal, 500);
    }

    public static Observable<Integer> slowly(final int start, final int goal, final int durationMilliSeconds) {
        final int offset = goal - start;
        final int count = 50;
        final int delay = durationMilliSeconds / count;
        final Observable<Integer> delayed = Observable.range(1, count)
                .concatMap(i -> Observable.just(i).delay(delay, TimeUnit.MILLISECONDS));
        if (offset == 0) {
            return delayed
                    .map(tick -> goal)
                    .observeOn(SwingScheduler.getInstance());
        }
        return delayed
                .map(tick -> {
                    if (tick == count) {
                        return goal;
                    } else {
                        final float rate = (float) (Math.pow(tick, 3) / Math.pow(count, 3));
                        return Math.round(start + offset * rate);
                    }
                })
                .observeOn(SwingScheduler.getInstance());
    }
}
