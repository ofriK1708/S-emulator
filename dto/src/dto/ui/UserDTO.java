package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying user information in the Users Panel
 */
public record UserDTO(StringProperty username, IntegerProperty mainProgramsUploaded,
                      IntegerProperty subFunctionsContributed, IntegerProperty currentCredits,
                      IntegerProperty creditsSpent, IntegerProperty totalRuns) {
}