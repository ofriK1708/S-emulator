package backend.engine;

public enum CommandType {
    BASIC("B"),
    SYNTHETIC("S");

    private final String symbol;

    CommandType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}