package engine.exception;

public class InstructionExecutionException extends RuntimeException {
    private final int remainingCredits;

    public InstructionExecutionException(String message, Throwable cause, int remainingCredits) {
        super(message, cause);
        this.remainingCredits = remainingCredits;
    }

    public int getRemainingCredits() {
        return remainingCredits;
    }
}