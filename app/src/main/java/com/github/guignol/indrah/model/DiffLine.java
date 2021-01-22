package com.github.guignol.indrah.model;

public class DiffLine {
    public final Diff diff;
    public final Hunk hunk;
    public final int indexInHunk;
    public final String line;

    public DiffLine(Diff diff, Hunk hunk, int indexInHunk, String line) {
        this.diff = diff;
        this.hunk = hunk;
        this.indexInHunk = indexInHunk;
        this.line = line;
    }

    public boolean belongsToSameHunk(DiffLine another) {
        return this.hunk.equals(another.hunk);
    }

    public boolean belongsToSameHunk(Hunk hunk) {
        return this.hunk.equals(hunk);
    }
}