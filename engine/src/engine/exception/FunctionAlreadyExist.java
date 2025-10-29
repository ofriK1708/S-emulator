package engine.exception;

import org.jetbrains.annotations.NotNull;

public class FunctionAlreadyExist extends Exception {
    public FunctionAlreadyExist(@NotNull String ProgramName, @NotNull String functionName) {
        super("Function '" + functionName + "' defined in program '" + ProgramName +
                "' already exists in the server, please choose a different name or file");
    }
}
