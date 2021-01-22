package com.github.guignol.indrah.mvvm.main_window;

public enum MainWindowType {
    COMMIT_MAKER("commit_maker"),
    COMMIT_BROWSER("commit_browser");

    public final String windowName;

    MainWindowType(String windowName) {
        this.windowName = windowName;
    }

    public MainWindowType next() {
        return next(this);
    }

    private static MainWindowType next(MainWindowType type) {
        switch (type) {
            case COMMIT_MAKER:
                return COMMIT_BROWSER;
            case COMMIT_BROWSER:
            default:
                return COMMIT_MAKER;
        }
    }
}
