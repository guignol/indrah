package com.github.guignol.indrah.model;

public class CommitLogAnnotated {
    public final CommitLog log;
    public final boolean lastJunction;

    public CommitLogAnnotated(CommitLog log, boolean lastJunction) {
        this.log = log;
        this.lastJunction = lastJunction;
    }
}
