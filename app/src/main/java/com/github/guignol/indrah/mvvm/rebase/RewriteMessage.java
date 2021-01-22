package com.github.guignol.indrah.mvvm.rebase;

import com.github.guignol.indrah.model.CommitLog;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RewriteMessage {

    private final Runnable onClose;

    private final PublishSubject<CommitLog> onMessageEdit = PublishSubject.create();

    public RewriteMessage(Runnable onClose) {
        this.onClose = onClose;
    }

    Observable<CommitLog> onMessageEdit() {
        return onMessageEdit.hide();
    }

    public void edit(CommitLog log) {
        onMessageEdit.onNext(log);
    }

    void close() {
        onClose.run();
    }
}
