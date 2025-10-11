package engine.exception;

public class FunctionAlreadyExist extends Exception {
    public FunctionAlreadyExist(String ProgramName, String functionName) {
        super("Function " + functionName + " defined in " + ProgramName +
                " already exists in the system, please choose a different name or file");
    }
}
