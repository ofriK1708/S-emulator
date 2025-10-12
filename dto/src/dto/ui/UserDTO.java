package dto.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 * DTO for displaying user information in the Users Panel
 */
public record UserDTO(StringProperty username, IntegerProperty mainProgramsUploaded,
                      IntegerProperty subFunctionsContributed, IntegerProperty currentCredits,
                      IntegerProperty creditsSpent, IntegerProperty totalRuns) {
}