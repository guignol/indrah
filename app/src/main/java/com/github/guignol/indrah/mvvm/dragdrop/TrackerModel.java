package com.github.guignol.indrah.mvvm.dragdrop;

import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class TrackerModel {

    ////////////////

    private final PublishSubject<Trackable> onTracking = PublishSubject.create();

    public void trackOn(Trackable trackable) {
        this.onTracking.onNext(trackable);
    }

    Observable<Trackable> onTracking() {
        return onTracking.hide();
    }

    ////////////////

    private final PublishSubject<Trackable> onTrackEnd = PublishSubject.create();

    public void trackEnd(Trackable trackable) {
        this.onTrackEnd.onNext(trackable);
    }

    Observable<Trackable> onTrackEnd() {
        return onTrackEnd.hide();
    }

    ////////////////

    private final EventStatus.Publisher onDrop = EventStatus.create();

    void doDrop() {
        onDrop.onNext();
    }

    public Observable<EventStatus> onDrop() {
        return onDrop.asObservable();
    }

    ////////////////
}
