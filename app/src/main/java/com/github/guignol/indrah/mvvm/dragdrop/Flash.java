package com.github.guignol.indrah.mvvm.dragdrop;

import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Flash {

    public static class Components {

        private final Component component;
        private final List<Component> optionals = new ArrayList<>();

        public Components(Component component) {
            this.component = component;
        }

        public void setVisible(boolean visible) {
            component.setVisible(visible);
            optionals.forEach(compo -> compo.setVisible(visible));
        }

        public void setBackground(Color c) {
            component.setBackground(c);
            optionals.forEach(compo -> compo.setBackground(c));
        }

        public Color getBackground() {
            return component.getBackground();
        }
    }

    private static final int initialDelay = 200;
    private static final int interval = 120;

    private final Components components;
    private Disposable disposable = null;
    private final AtomicBoolean lock = new AtomicBoolean(false);

    public Flash(Component component) {
        this.components = new Components(component);
    }

    public void auto(boolean visibility, int duration) {
        // 自動運転中は外部から干渉させない
        lock.set(true);
        stopGracefully();
        start();
        // 自動停止タイマー
        Single.timer(duration, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    lock.set(false);
                    stopGracefully(visibility);
                });
    }

    public void startWith(@Nullable List<? extends Component> optionals) {
        components.optionals.clear();
        if (optionals != null) {
            components.optionals.addAll(optionals);
        }
        start();
    }

    private void start() {
        if (disposable != null) {
            return;
        }
        components.setVisible(true);
        final AtomicBoolean stop = new AtomicBoolean(false);
        final Color original = components.getBackground();
        disposable = Observable.interval(initialDelay, interval, TimeUnit.MILLISECONDS, SwingScheduler.getInstance())
                .doOnDispose(() -> components.setBackground(original))
                .subscribe(counter -> {
                    if (++counter % 7 == 0) {
                        if (!stop.getAndSet(!stop.get())) {
                            components.setBackground(original);
                        }
                    }
                    if (stop.get()) {
                        return;
                    }
                    if (counter % 2 == 0) {
                        components.setBackground(original.brighter());
                    } else {
                        components.setBackground(original.darker());
                    }
                });
    }

    private void stopForcibly() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    public void stopGracefully() {
        if (lock.get()) {
            return;
        }
        stopForcibly();
    }

    public void stopGracefully(boolean visibility) {
        if (lock.get()) {
            return;
        }
        this.stopGracefully();
        components.setVisible(visibility);
    }
}
