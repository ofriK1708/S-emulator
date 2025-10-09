package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying function information in the Functions Panel
 */
public class FunctionDTO {
    private final StringProperty functionName;
    private final StringProperty programName;
    private final StringProperty uploadedBy;
    private final IntegerProperty instructionCount;
    private final IntegerProperty maxExpandLevel;

    public FunctionDTO(String functionName, String programName, String uploadedBy,
                       int instructionCount, int maxExpandLevel) {
        this.functionName = new SimpleStringProperty(functionName);
        this.programName = new SimpleStringProperty(programName);
        this.uploadedBy = new SimpleStringProperty(uploadedBy);
        this.instructionCount = new SimpleIntegerProperty(instructionCount);
        this.maxExpandLevel = new SimpleIntegerProperty(maxExpandLevel);
    }

    // Getters for properties
    public StringProperty functionNameProperty() { return functionName; }
    public StringProperty programNameProperty() { return programName; }
    public StringProperty uploadedByProperty() { return uploadedBy; }
    public IntegerProperty instructionCountProperty() { return instructionCount; }
    public IntegerProperty maxExpandLevelProperty() { return maxExpandLevel; }

    // Value getters
    public String getFunctionName() { return functionName.get(); }
    public String getProgramName() { return programName.get(); }
    public String getUploadedBy() { return uploadedBy.get(); }
    public int getInstructionCount() { return instructionCount.get(); }
    public int getMaxExpandLevel() { return maxExpandLevel.get(); }
}