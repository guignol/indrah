package com.github.guignol.indrah.model;

public enum DiffStatus {
    // Added (A), Copied (C), Deleted (D), Modified (M), Renamed (R)
    Added,
    //    Copied,
    Deleted,
    Modified,
    Renamed,
    Other;

    public static DiffStatus from(String statusLine) {
        if (statusLine.startsWith("A")) {
            return Added;
//        } else if (statusLine.startsWith("C")) {
//            return Copied;
        } else if (statusLine.startsWith("D")) {
            return Deleted;
        } else if (statusLine.startsWith("M")) {
            return Modified;
        } else if (statusLine.startsWith("R")) {
            return Renamed;
        } else {
            return Other;
        }
    }

    public DiffSummary withNames(String before, String after) {
        return new DiffSummary(this, new DiffNames(before, after));
    }
}
