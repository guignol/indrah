package com.github.guignol.indrah.mvvm.dragdrop;

import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;

class TrackerViewModel extends IViewModel<TrackerModel> {

    TrackerViewModel(TrackerModel model) {
        super(model);
    }
    /////////// from View

    void doDrop() {
        model.doDrop();
    }

    /////////// to View

    Observable<Trackable> onTracked() {
        return model.onTracking().observeOn(SwingScheduler.getInstance());
    }

    Observable<Trackable> onTrackEnd() {
        return model.onTrackEnd().observeOn(SwingScheduler.getInstance());
    }
}
