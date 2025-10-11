package dto.ui;

import javafx.beans.property.*;

/**
 * DTO for displaying execution history records in the History Panel
 */
public record HistoryRecordDTO(IntegerProperty runId, StringProperty executionType, StringProperty programFunctionName,
                               StringProperty architectureType, IntegerProperty expandLevel,
                               IntegerProperty finalYValue, IntegerProperty cycleCount) {
}