package logic;

import dto.server.UserDTO;
import engine.core.ProgramDebugger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class User {
    private @NotNull
    final String name;
    private int mainProgramsUploaded = 0;
    private int subFunctionsContributed = 0;
    private int currentCredits = Integer.MAX_VALUE; // TODO - change this later to 0, only for testing
    private int usedCredits = 0;
    private int totalRuns = 0;
    private @Nullable ProgramDebugger debugger = null;

    public User(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setDebugger(@NotNull ProgramDebugger debugger) {
        if (this.debugger != null) {
            throw new IllegalStateException("Debugger is already set for user: " + name);
        }
        this.debugger = debugger;
    }

    public void clearDebugger() {
        this.debugger = null;
    }

    public @Nullable ProgramDebugger getDebugger() {
        return debugger;
    }

    public int getCurrentCredits() {
        return currentCredits;
    }

    public void addFunctionsCount(int subFunctionsContributed) {
        this.subFunctionsContributed += subFunctionsContributed;
    }

    public void incrementMainProgramsUploaded() {
        this.mainProgramsUploaded++;
    }

    public void addCredits(int currentCredits) {
        this.currentCredits += currentCredits;
    }

    public void chargeCredits(int chargedCredits) {
        this.currentCredits -= chargedCredits;
        this.usedCredits += chargedCredits;
    }

    /**
     * Set the remaining credits of the user after an insufficient credits exception.
     *
     * @param remainingCredits the remaining credits to set
     */
    public void setRemainingCredits(int remainingCredits) {
        this.usedCredits = this.usedCredits + (this.currentCredits - remainingCredits);
        this.currentCredits = remainingCredits;
    }

    public void increaseTotalRuns() {
        this.totalRuns++;
    }

    public @NotNull UserDTO getUserDTO() {
        return new UserDTO(
                name,
                mainProgramsUploaded,
                subFunctionsContributed,
                currentCredits,
                usedCredits,
                totalRuns
        );
    }

    public int getTotalRuns() {
        return totalRuns;
    }
}
