package engine.exception;

public class FunctionNotFound extends Exception {
    public FunctionNotFound(int instructionIndex, String instruction, String atFunction, String unknownFunctionCalled) {
        super(String.format("Error at instruction #%d %s at function/program '%s': " +
                        "trying to call '%s'. This function does not exist in the program or in the system.",
                instructionIndex + 1, instruction, atFunction, unknownFunctionCalled));

    }
}
// "Error at the #%d Instruction at function/program '%s': " + "trying to call '%s'. " +
//                        "This function does not exist in the program.",
//                instructionIndex + 1, atFunction, unknownFunctionCalled
