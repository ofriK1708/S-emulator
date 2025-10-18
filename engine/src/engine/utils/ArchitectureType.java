package engine.utils;

public enum ArchitectureType {
    ARCHITECTURE_I("I", 5),
    ARCHITECTURE_II("II", 100),
    ARCHITECTURE_III("III", 500),
    ARCHITECTURE_IV("IV", 1000);

    private final String symbol;
    private final int creditsCost;

    ArchitectureType(String symbol, int creditsCost) {
        this.symbol = symbol;
        this.creditsCost = creditsCost;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getCreditsCost() {
        return creditsCost;
    }
}
