package dto.server;

public record UserDTO(String name,
                      int mainProgramsUploaded,
                      int subFunctionsContributed,
                      int currentCredits,
                      int usedCredits,
                      int totalRuns) {
}
