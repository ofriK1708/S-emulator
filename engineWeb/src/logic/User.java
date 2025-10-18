package logic;

import org.jetbrains.annotations.NotNull;

public record User(@NotNull String name,
                   int mainProgramsUploaded,
                   int subFunctionsContributed,
                   int currentCredits,
                   int usedCredits,
                   int totalRuns) {
}
