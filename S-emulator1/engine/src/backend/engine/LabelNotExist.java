package backend.engine;

public class LabelNotExist extends RuntimeException
{
    public LabelNotExist(String InstructionName, int lineNumber, String labelName)
    {
        super(String.format("Error at the #%d Instruction '%s': " +
                        "Label '%s' does not exist in the program.",
                lineNumber, InstructionName, labelName));
    }
}
