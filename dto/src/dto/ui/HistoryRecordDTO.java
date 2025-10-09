package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying execution history records in the History Panel
 */
public class HistoryRecordDTO {
    private final IntegerProperty runId;
    private final StringProperty executionType;
    private final StringProperty programFunctionName;
    private final StringProperty architectureType;
    private final IntegerProperty expandLevel;
    private final IntegerProperty finalYValue;
    private final IntegerProperty cycleCount;

    public HistoryRecordDTO(int runId, String executionType, String programFunctionName,
                            String architectureType, int expandLevel, int finalYValue,
                            int cycleCount) {
        this.runId = new SimpleIntegerProperty(runId);
        this.executionType = new SimpleStringProperty(executionType);
        this.programFunctionName = new SimpleStringProperty(programFunctionName);
        this.architectureType = new SimpleStringProperty(architectureType);
        this.expandLevel = new SimpleIntegerProperty(expandLevel);
        this.finalYValue = new SimpleIntegerProperty(finalYValue);
        this.cycleCount = new SimpleIntegerProperty(cycleCount);
    }

    // Property getters
    public IntegerProperty runIdProperty() { return runId; }
    public StringProperty executionTypeProperty() { return executionType; }
    public StringProperty programFunctionNameProperty() { return programFunctionName; }
    public StringProperty architectureTypeProperty() { return architectureType; }
    public IntegerProperty expandLevelProperty() { return expandLevel; }
    public IntegerProperty finalYValueProperty() { return finalYValue; }
    public IntegerProperty cycleCountProperty() { return cycleCount; }

    // Value getters
    public int getRunId() { return runId.get(); }
    public String getExecutionType() { return executionType.get(); }
    public String getProgramFunctionName() { return programFunctionName.get(); }
    public String getArchitectureType() { return architectureType.get(); }
    public int expandLevelLevel() { return expandLevel.get(); }
    public int getFinalYValue() { return finalYValue.get(); }
    public int getCycleCount() { return cycleCount.get(); }
}