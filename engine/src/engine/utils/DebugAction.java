package engine.utils;

public enum DebugAction {
    STEP_OVER("step_over"),
    STEP_BACK("step_back"),
    RESUME("resume"),
    STOP("pause");

    public final String debugActionString;

    DebugAction(String debugActionString) {
        this.debugActionString = debugActionString;
    }

    /**
     * A case-insensitive version of valueOf.
     *
     * @param action The string to convert.
     * @return The matching DebugAction, or null if no match is found.
     */
    public static DebugAction fromString(String action) {
        if (action == null) {
            return null;
        }
        try {
            return DebugAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return debugActionString;
    }
}

