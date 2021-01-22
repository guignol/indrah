package com.github.guignol.indrah.mvvm.commit;

import com.github.guignol.indrah.command.ConfigCommand;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class GitUser {
    @NotNull
    public final String name;
    @NotNull
    public final String email;

    public GitUser(@NotNull String name, @NotNull String email) {
        this.name = name;
        this.email = email;
    }

    public static class Viewer {

        public static Single<GitUser> local(@NotNull Path dir) {
            final ConfigCommand config = ConfigCommand.with(dir);
            final Single<String> userName = config.local("user.name");
            final Single<String> userEmail = config.local("user.email");
            return Single.zip(userName, userEmail, GitUser::new);
        }

        public static Single<GitUser> global(@NotNull Path dir) {
            final ConfigCommand config = ConfigCommand.with(dir);
            final Single<String> userName = config.global("user.name");
            final Single<String> userEmail = config.global("user.email");
            return Single.zip(userName, userEmail, GitUser::new);
        }
    }

    public static class Editor {

        public static Single<GitUser> local(@NotNull Path dir, GitUser user) {
            final ConfigCommand config = ConfigCommand.with(dir);
            final Single<String> userName = config.local("user.name", user.name);
            final Single<String> userEmail = config.local("user.email", user.email);
            return Single.zip(userName, userEmail, GitUser::new);
        }

        public static Single<GitUser> global(@NotNull Path dir, GitUser user) {
            final ConfigCommand config = ConfigCommand.with(dir);
            final Single<String> userName = config.global("user.name", user.name);
            final Single<String> userEmail = config.global("user.email", user.email);
            return Single.zip(userName, userEmail, GitUser::new);
        }
    }
}
