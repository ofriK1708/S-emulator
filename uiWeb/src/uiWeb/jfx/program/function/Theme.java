package uiWeb.jfx.program.function;

public enum Theme {
    LIGHT("Light"),
    DARK("Dark"),
    MAC_IS_THE_BEST("mac"),
    WINDOWS_IS_THE_WORST("windows");

    private final String displayName;

    Theme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
