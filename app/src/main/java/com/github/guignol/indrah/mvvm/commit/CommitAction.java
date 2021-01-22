package com.github.guignol.indrah.mvvm.commit;

import com.github.guignol.indrah.command.CommitCommand;
import com.github.guignol.indrah.command.CommitMessageCommand;
import com.github.guignol.indrah.utils.StringUtils;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;

public class CommitAction {

    private final Supplier<Path> dir;

    public CommitAction(@NotNull Supplier<Path> dir) {
        this.dir = dir;
    }

    private final PublishSubject<String> onMessageOfHEAD = PublishSubject.create();

    Observable<String> onMessageOfHEAD() {
        return onMessageOfHEAD.hide();
    }

    void fetchMessageOfHEAD() {
        final Path path = dir.get();
        if (path == null) {
            return;
        }
        new CommitMessageCommand(path)
                .toSingle()
                .map(output -> output.standardInputs.stream().collect(StringUtils.WITH_LINE_BREAK))
                .subscribe(onMessageOfHEAD::onNext);
    }

    private final EventStatus.Publisher onCommit = EventStatus.create();

    public Observable<EventStatus> onCommit() {
        return onCommit.asObservable();
    }

    public void commit(CommitOptions options) {
        final Path path = dir.get();
        if (path == null) {
            return;
        }
        final CommitCommand command;
        if (options.message == null) {
            command = CommitCommand.noEdit(path);
        } else if (options.amend) {
            command = CommitCommand.amend(path, options.message);
        } else if (options.allowEmpty) {
            command = CommitCommand.allowEmpty(path, options.message);
        } else {
            command = CommitCommand.newly(path, options.message);
        }
        command.toSingle().subscribe(output -> {
            output.print();
            onCommit.onNext();
        });
    }

    ////// TODO user.nameとuser.email

    private final PublishSubject<GitUser> onGlobalUser = PublishSubject.create();
    private final PublishSubject<GitUser> onLocalUser = PublishSubject.create();

    private Observable<GitUser> onGlobalUser() {
        return onGlobalUser.hide();
    }

    private Observable<GitUser> onLocalUser() {
        return onLocalUser.hide();
    }

    Observable<GitUser> onGitUser() {
        return Observable.zip(onGlobalUser().hide(), onLocalUser().hide(), (global, local) -> new GitUser(
                !local.name.isEmpty()
                        ? local.name
                        : global.name,
                !local.email.isEmpty()
                        ? local.email
                        : global.email));
    }

    public void fetchUser() {
        final Path path = dir.get();
        if (path == null) {
            return;
        }
        GitUser.Viewer.global(path).subscribe(onGlobalUser::onNext);
        GitUser.Viewer.local(path).subscribe(onLocalUser::onNext);
    }

    ////// expand/shrink

    @Nullable
    public Runnable expand = null;
    @Nullable
    public Runnable shrink = null;

    private boolean expanded = false;

    public void shrink() {
        expanded = true;
        resize();
    }

    public void resize() {
        if (expanded) {
            if (shrink != null) {
                shrink.run();
            }
            onShrink.onNext();
        } else {
            if (expand != null) {
                expand.run();
            }
            onExpand.onNext();
        }
        expanded = !expanded;
    }

    private final EventStatus.Publisher onExpand = EventStatus.create();

    Observable<EventStatus> onExpand() {
        return onExpand.asObservable();
    }

    private EventStatus.Publisher onShrink = EventStatus.create();

    Observable<EventStatus> onShrink() {
        return onShrink.asObservable();
    }
}
