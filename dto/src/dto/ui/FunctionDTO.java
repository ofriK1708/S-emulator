package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying function information in the Functions Panel
 */
public record FunctionDTO(StringProperty functionName, StringProperty programName, StringProperty uploadedBy,
                          IntegerProperty instructionCount, IntegerProperty maxExpandLevel) {
}