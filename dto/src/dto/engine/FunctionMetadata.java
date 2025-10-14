package dto.engine;

public record FunctionMetadata(String name, String ProgramContext, String uploadedBy, int numOfInstructions,
                               int maxLevel) {
}
