package engine.exception;

public class FunctionNotFound extends Exception {
    public FunctionNotFound(int instructionIndex, String atFunction, String unknownFunctionCalled) {
        super(String.format("Error at the #%d Instruction at function/program '%s': " + "trying to call '%s'. " +
                        "This function does not exist in the program.",
                instructionIndex + 1, atFunction, unknownFunctionCalled));

    }
}
