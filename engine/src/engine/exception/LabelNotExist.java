package engine.exception;

public class LabelNotExist extends Exception
{
    public LabelNotExist(String InstructionName, int InstructionNumber, String labelName)
    {
        super(String.format("Error at the #%d Instruction '%s': " +
                        "Label '%s' does not exist in the program.",
                InstructionNumber, InstructionName, labelName));
    }
}
