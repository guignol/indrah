package com.github.guignol.indrah.mvvm.commit;

import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.utils.StringUtils;
import com.github.guignol.swing.binding.BindableViewModel;
import com.github.guignol.swing.binding.IViewModel;
import com.github.guignol.swing.binding.Notification;
import com.github.guignol.swing.rx.EventStatus;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import java.awt.event.ActionEvent;

import static com.github.guignol.indrah.mvvm.commit.CommitViewModel.TEXT.COMMIT_BUTTON;
import static com.github.guignol.indrah.mvvm.commit.CommitViewModel.TEXT.COMMIT_MESSAGE;
import static com.github.guignol.swing.rx.EventStatus.NEXT;

public class CommitViewModel extends IViewModel<CommitAction> {

    public CommitViewModel(CommitAction model) {
        super(model);
    }

    /////////// from View

    // TODO ただの変数でよさそう
    private final ValueHolder<String> inputMessageCache = ValueHolder.create();
    final Consumer<String> inputMessage = inputMessageCache::put;

    private final ValueHolder<Boolean> amendSelected = ValueHolder.create();
    final Consumer<Boolean> amend = selected -> {
        amendSelected.put(selected);
        // TODO clear messageなボタンが欲しい
        if (selected && StringUtils.isBlank(inputMessageCache.get())) {
            model.fetchMessageOfHEAD();
        }
    };

    private final ValueHolder<Boolean> noEditSelected = ValueHolder.create();
    final Consumer<Boolean> noEdit = selected -> {
        noEditSelected.put(selected);
        if (selected) {
            model.fetchMessageOfHEAD();
        }
    };

    private final ValueHolder<Boolean> allowEmptySelected = ValueHolder.create();
    final Consumer<Boolean> allowEmpty = allowEmptySelected::put;

    final Consumer<ActionEvent> commitButtonAction = actionEvent -> {
        if (noEditSelected.get()) {
            model.commit(new CommitOptions(null, false, false));
        } else {
            if (StringUtils.isBlank(inputMessageCache.get())) {
                // TODO アラートを出す？
                return;
            }
            model.commit(new CommitOptions(inputMessageCache.get(), allowEmptySelected.get(), amendSelected.get()));
        }
    };

    /////////// to View

    enum ENABLED implements Notification.FromValue<ENABLED, Boolean> {
        INPUT_MESSAGE,
        ALLOW_EMPTY,
        AMEND,
        NO_EDIT
    }

    enum TEXT implements Notification.FromValue<TEXT, String> {
        COMMIT_BUTTON,
        COMMIT_MESSAGE
    }

    enum EVENT implements Notification.FromValue<EVENT, EventStatus> {
        COMMIT,
        EXPAND,
        SHRINK
    }

    private final Observable<EventStatus> statusChanged = Observable
            .combineLatest(
                    allowEmptySelected.observable(),
                    amendSelected.observable(),
                    noEditSelected.observable(),
                    (bool_1, bool_2, bool_3) -> NEXT);

    BindableViewModel<Boolean> bindEnabled(ENABLED... filter) {
        return Notification.getBinder(enabled, filter);
    }

    private final Observable<Notification<ENABLED, Boolean>> enabled = statusChanged.flatMap(eventStatus -> {
        // TODO 取得はここでいいの？
        final boolean noMessage = noEditSelected.get();
        final boolean noIndex = allowEmptySelected.get();
        return Observable.fromArray(ENABLED.values())
                .map(control -> {
                    switch (control) {
                        case INPUT_MESSAGE:
                            return control.notify(!noMessage);
                        case ALLOW_EMPTY:
                            return control.notify(!noMessage);
                        case AMEND:
                            return control.notify(!noMessage && !noIndex);
                        case NO_EDIT:
                        default:
                            return control.notify(!noIndex);
                    }
                });
    });

    BindableViewModel<String> bindText(TEXT... filter) {
        return Notification.getBinder(text, filter);
    }

    private final Observable<Notification<TEXT, String>> text = Observable.merge(
            Observable.create(e ->
                    model.onMessageOfHEAD()
                            .observeOn(SwingScheduler.getInstance())
                            .subscribe(s -> e.onNext(COMMIT_MESSAGE.notify(s)))),
            statusChanged.map(eventStatus -> {
                // ボタン名の更新
                final String result;
                if (allowEmptySelected.get()) {
                    result = "git commit --allow-empty";
                } else if (noEditSelected.get()) {
                    result = "git commit --amend --no-edit";
                } else if (amendSelected.get()) {
                    result = "git commit --amend";
                } else {
                    result = "git commit";
                }
                return COMMIT_BUTTON.notify(result);
            }));


    BindableViewModel<EventStatus> bindEvent(EVENT... filter) {
        return Notification.getBinder(event, filter);
    }

    private final Observable<Notification<EVENT, EventStatus>> event = Observable.create(emitter -> {
        // コミット後
        model.onCommit()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(eventStatus -> {
                    emitter.onNext(EVENT.COMMIT.notify(NEXT));
                    model.shrink();
                });
        // 拡大表示後
        model.onExpand()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(eventStatus -> emitter.onNext(EVENT.EXPAND.notify(NEXT)));
        // 縮小表示後
        model.onShrink()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(eventStatus -> emitter.onNext(EVENT.SHRINK.notify(NEXT)));
    });

    //////////////////////////////

    Observable<GitUser> onGitUser() {
        return model.onGitUser()
                .observeOn(SwingScheduler.getInstance());
    }
}