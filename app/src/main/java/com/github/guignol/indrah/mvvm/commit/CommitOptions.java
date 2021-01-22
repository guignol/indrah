package com.github.guignol.indrah.mvvm.commit;

import org.jetbrains.annotations.Nullable;

public class CommitOptions {
    @Nullable
    public final String message;
    public final boolean allowEmpty;
    public final boolean amend;

    public CommitOptions(@Nullable String message, boolean allowEmpty, boolean amend) {
        this.message = message;
        this.allowEmpty = allowEmpty;
        this.amend = amend;
    }
}
