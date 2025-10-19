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
    private int currentCredits = Integer.MIN_VALUE; // TODO - change this later to 0, only for testing
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

    public int getCurrentCredits() {
        return currentCredits;
    }

    public void addProgramsAndFunctions(int mainProgramsUploaded, int subFunctionsContributed) {
        this.mainProgramsUploaded += mainProgramsUploaded;
        this.subFunctionsContributed += subFunctionsContributed;
    }

    public void addCredits(int currentCredits) {
        this.currentCredits += currentCredits;
    }

    public void chargeCredits(int chargedCredits) {
        this.currentCredits -= chargedCredits;
        this.usedCredits += chargedCredits;
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
}
