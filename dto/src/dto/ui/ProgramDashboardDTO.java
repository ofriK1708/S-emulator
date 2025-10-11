package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying program information in the Program Dashboard
 */
public record ProgramDashboardDTO(StringProperty programName, StringProperty uploadedBy,
                                  IntegerProperty instructionCount, IntegerProperty maxExpandLevel,
                                  IntegerProperty totalRuns, DoubleProperty avgCreditCost) {
}