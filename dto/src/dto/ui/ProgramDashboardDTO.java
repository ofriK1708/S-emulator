package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying program information in the Programs Panel
 * Note: This is different from dto.engine.ProgramDTO which is for engine operations
 */
public class ProgramDashboardDTO {
    private final StringProperty programName;
    private final StringProperty uploadedBy;
    private final IntegerProperty instructionCount;
    private final IntegerProperty maxExpandLevel;
    private final IntegerProperty totalRuns;
    private final DoubleProperty avgCreditCost;

    public ProgramDashboardDTO(String programName, String uploadedBy, int instructionCount,
                               int maxExpandLevel, int totalRuns, double avgCreditCost) {
        this.programName = new SimpleStringProperty(programName);
        this.uploadedBy = new SimpleStringProperty(uploadedBy);
        this.instructionCount = new SimpleIntegerProperty(instructionCount);
        this.maxExpandLevel = new SimpleIntegerProperty(maxExpandLevel);
        this.totalRuns = new SimpleIntegerProperty(totalRuns);
        this.avgCreditCost = new SimpleDoubleProperty(avgCreditCost);
    }

    // Property getters
    public StringProperty programNameProperty() { return programName; }
    public StringProperty uploadedByProperty() { return uploadedBy; }
    public IntegerProperty instructionCountProperty() { return instructionCount; }
    public IntegerProperty maxExpandLevelProperty() { return maxExpandLevel; }
    public IntegerProperty totalRunsProperty() { return totalRuns; }
    public DoubleProperty avgCreditCostProperty() { return avgCreditCost; }

    // Value getters
    public String getProgramName() { return programName.get(); }
    public String getUploadedBy() { return uploadedBy.get(); }
    public int getInstructionCount() { return instructionCount.get(); }
    public int getMaxExpandLevel() { return maxExpandLevel.get(); }
    public int getTotalRuns() { return totalRuns.get(); }
    public double getAvgCreditCost() { return avgCreditCost.get(); }
}