package com.github.guignol.indrah.model;

public class DiffSummary {
    public final DiffStatus status;
    public final DiffNames names;

    public DiffSummary(DiffStatus status, DiffNames names) {
        this.status = status;
        this.names = names;
    }
}
