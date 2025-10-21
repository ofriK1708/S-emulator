package engine.exception;

public class InsufficientCredits extends RuntimeException {
    private final int creditsLeft;

    public InsufficientCredits(String message, int creditsLeft, int requiredCredits) {
        super(String.format("%s: current available credits %d, but required %d", message, creditsLeft,
                requiredCredits));
        this.creditsLeft = creditsLeft;
    }

    public int getCreditsLeft() {
        return creditsLeft;
    }
}
