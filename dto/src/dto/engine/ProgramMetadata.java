package dto.engine;

public record ProgramMetadata(String name, String uploadedBy, int numOfInstructions, int maxLevel,
                              int numberOfExecutions,
                              float averageCycles) {
}
