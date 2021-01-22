package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;

public class WindowSwitcher {

    //////////////////////

    private final EventStatus.Publisher onRequested = EventStatus.create();

    public Observable<EventStatus> onRequested() {
        return onRequested.asObservable();
    }

    void changeWindow() {
        onRequested.onNext();
    }
}
