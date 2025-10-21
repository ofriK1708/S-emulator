package engine.exception;

public class ExpandLevelOutOfBounds extends IllegalArgumentException {
    public ExpandLevelOutOfBounds(String message, int level, int minLevel, int maxLevel) {
        super(String.format("%s: level %d is out of bounds (%d to %d)", message, level, minLevel, maxLevel));
    }
}
