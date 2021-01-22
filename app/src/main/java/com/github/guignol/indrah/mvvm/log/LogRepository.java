package com.github.guignol.indrah.mvvm.log;

import com.github.guignol.indrah.command.LogCommand;
import com.github.guignol.indrah.model.CommitLogHistory;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Supplier;

public class LogRepository {

    private final Supplier<Path> directory;

    public LogRepository(@NotNull Supplier<Path> directory) {
        this.directory = directory;
    }

    private final PublishSubject<CommitLogHistory> loaded = PublishSubject.create();

    public Observable<CommitLogHistory> asObservable() {
        return loaded.hide();
    }

    public void load() {
        final Path path = directory.get();
        if (path == null) {
            return;
        }
        // TODO 詳細は後でとる。詳細はキャッシュ可能。
        new LogCommand(path).toSingle().subscribe(output -> {
//            output.print();
            if (output.exitCode == 0 && !output.standardInputs.isEmpty()) {
                // TODO 親を辿れるように連結する
                final CommitLogHistory history = CommitLogHistory.parse(output.standardInputs);
//                logs.forEach(CommitLog::print);
                loaded.onNext(history);
            }
        });
    }
}
