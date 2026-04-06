package com.blackjackengine.ui;

public enum UiTheme {
    CLASSIC_GREEN("Classic Green", "theme-classic"),
    LUXURY_GOLD("Luxury Gold", "theme-luxury"),
    NEON_CASINO("Neon Casino", "theme-neon");

    private final String displayName;
    private final String styleClass;

    UiTheme(String displayName, String styleClass) {
        this.displayName = displayName;
        this.styleClass = styleClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public static UiTheme fromName(String name) {
        if (name == null || name.isBlank()) {
            return CLASSIC_GREEN;
        }

        for (UiTheme theme : values()) {
            if (theme.name().equalsIgnoreCase(name) || theme.displayName.equalsIgnoreCase(name)) {
                return theme;
            }
        }

        return CLASSIC_GREEN;
    }

    public UiTheme next() {
        UiTheme[] themes = values();
        return themes[(ordinal() + 1) % themes.length];
    }
}
