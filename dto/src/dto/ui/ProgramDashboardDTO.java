package dto.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 * DTO for displaying program information in the Program Dashboard
 */
public record ProgramDashboardDTO(StringProperty programName, StringProperty uploadedBy,
                                  IntegerProperty instructionCount, IntegerProperty maxExpandLevel,
                                  IntegerProperty totalRuns, DoubleProperty avgCreditCost) {
}