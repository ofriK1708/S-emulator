package backend.engine.syntheticCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class JumpZero extends Instruction
{
    private final String labelArgumentName = "JZLabel";

    public JumpZero(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String labelName = args.get(labelArgumentName);
        if (contextMap.containsKey(labelName))
        {
            int value = contextMap.get(mainVarName);
            int labelLineNumber = contextMap.get(labelName);
            if (value != 0)
            {
                contextMap.put(ProgramCounterName, contextMap.get(ProgramCounterName) + 1);
            } else
            {
                contextMap.put(ProgramCounterName, labelLineNumber);
            }
        } else
        {
            throw new IllegalArgumentException("No such label : " + labelName);
        }
    }

    @Override
    public List<Command> expand(int level) {
        return List.of();
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getNumberOfArgs() {
        return 1;
    }

    @Override
    public String getDisplayFormat(int instructionNumber) {
        String labelName = args.get(labelArgumentName);
        String commandPart = String.format("if %s == 0 GOTO %s", mainVarName, labelName);
        return formatDisplay(instructionNumber, commandPart);
    }
}
