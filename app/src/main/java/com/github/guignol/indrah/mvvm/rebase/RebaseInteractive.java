package com.github.guignol.indrah.mvvm.rebase;

import com.github.guignol.indrah.command.Command;
import com.github.guignol.indrah.command.RebaseInteractiveCommand;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RebaseInteractive {

    private static AtomicBoolean inProgress = new AtomicBoolean(false);

    /**
     * sequence.editorとcore.editorでオプションを区別したいが実行ファイル名しか渡せない
     * SourceTreeはファイル名かファイル内容だけで区別してるっぽい
     * git -c diff.mnemonicprefix=false -c core.quotepath=false -c sequence.editor='%homepath%\AppData\Local\SourceTree\app-2.3.5\tools\stree_gri' -c core.editor='%homepath%\AppData\Local\SourceTree\app-2.3.5\tools\stree_gri' rebase -i --autosquash 115a2e7a4d5a44c8b1cabebbc66a6322c6141578
     *
     * 【sequence.editor】 rebase -iの最初の編集
     * Text editor used by git rebase -i for editing the rebase instruction file.
     * https://git-scm.com/docs/git-config#git-config-sequenceeditor
     *
     * 【core.editor】 rewordでコミットメッセージを修正するとき
     * Commands such as commit and tag that let you edit messages by launching an editor
     * https://git-scm.com/docs/git-config#git-config-coreeditor
     *
     * TODO https://stackoverflow.com/questions/10942427/how-to-have-2-jvms-talk-to-one-another
     * TODO https://github.com/netty/netty/tree/4.0/example/src/main/java/io/netty/example
     * TODO http://aoe-tk.hatenablog.com/entry/2015/12/19/170651
     */
    public static Single<EventStatus> start(@Nullable Function<Integer, Command> factory,
                                            @Nullable Consumer<Path> pathHandler) {
        if (factory == null || pathHandler == null) {
            return Single.never();
        }
        if (inProgress.get()) {
            System.out.println("return because git rebase -i is in progress");
            return Single.never();
        }
        inProgress.set(true);
        return new RebaseProxyServer()
                .start(factory, pathHandler)
                .subscribeOn(Schedulers.io())
                .doOnEvent((eventStatus, throwable) -> inProgress.set(false));
    }


    private final Supplier<Path> directory;
    private final String sequenceEditor;
    private final Runnable doReload;

    public RebaseInteractive(Supplier<Path> directory, String sequenceEditor, Runnable doReload) {
        this.directory = directory;
        this.sequenceEditor = sequenceEditor;
        this.doReload = doReload;
    }

    public void start(RebaseTodo rebaseTodo) {
        final Function<Integer, Command> factory = getFactory(rebaseTodo.ontoCommit);
        RebaseInteractive.start(factory, rebaseTodo.pathHandler()).subscribe(eventStatus -> doReload.run());
    }

    @Nullable
    private Function<Integer, Command> getFactory(String targetCommit) {
        final Path path = directory.get();
        if (path == null) {
            return null;
        }
        return port -> new RebaseInteractiveCommand(path, String.valueOf(port), targetCommit, sequenceEditor);
    }
}
