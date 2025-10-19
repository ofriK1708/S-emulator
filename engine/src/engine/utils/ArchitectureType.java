package engine.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum ArchitectureType {
    ARCHITECTURE_I("I", 5),
    ARCHITECTURE_II("II", 100),
    ARCHITECTURE_III("III", 500),
    ARCHITECTURE_IV("IV", 1000),
    INNER_RUN_ARCHITECTURE("Inner Run", 0);

    private final String symbol;
    private final int creditsCost;

    ArchitectureType(String symbol, int creditsCost) {
        this.symbol = symbol;
        this.creditsCost = creditsCost;
    }

    public static boolean isValidArchitectureType(String architectureTypeStr) {
        for (ArchitectureType architectureType : ArchitectureType.values()) {
            if (architectureType.symbol.equals(architectureTypeStr)) {
                return true;
            }
        }
        return false;
    }

    public static ArchitectureType fromString(String architectureTypeStr) {
        for (ArchitectureType architectureType : ArchitectureType.values()) {
            if (architectureType.symbol.equals(architectureTypeStr)) {
                return architectureType;
            }
        }
        throw new IllegalArgumentException("Invalid architecture type: " + architectureTypeStr);
    }

    public static @NotNull List<String> getSupportedArchitectures() {
        List<String> supportedArchitectures = new ArrayList<>();
        for (ArchitectureType architectureType : ArchitectureType.values()) {
            if (!architectureType.equals(INNER_RUN_ARCHITECTURE)) {
                supportedArchitectures.add(architectureType.getSymbol());
            }
        }
        return supportedArchitectures;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getCreditsCost() {
        return creditsCost;
    }
}
