package dto.server;

/**
 * Data Transfer Object (DTO) representing a user in the system.
 *
 * @param name                    The name of the user.
 * @param mainProgramsUploaded    The number of main programs uploaded by the user.
 * @param subFunctionsContributed The number of sub-functions contributed by the user.
 * @param currentCredits          The current credits available to the user.
 * @param creditSpend             The total credits used by the user.
 * @param totalRuns               The total number of program runs executed by the user.
 */
public record UserDTO(String name,
                      int mainProgramsUploaded,
                      int subFunctionsContributed,
                      int currentCredits,
                      int creditSpend,
                      int totalRuns) {
}
