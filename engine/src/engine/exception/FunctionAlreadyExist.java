package engine.exception;

public class FunctionAlreadyExist extends Exception {
    public FunctionAlreadyExist(String ProgramName, String functionName) {
        super("Function " + functionName + " already defined in " + ProgramName);
    }
}
