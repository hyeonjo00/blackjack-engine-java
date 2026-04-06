package com.blackjackengine.ui;

public interface AppScreenController {
    void setApplication(BlackjackApplication application);

    default void onThemeChanged(UiTheme theme) {
        // Default no-op for screens that only rely on CSS theme classes.
    }

    default void onViewActivated() {
        // Default no-op for screens without activation hooks.
    }

    default void onViewDeactivated() {
        // Default no-op for screens without teardown hooks.
    }
}
