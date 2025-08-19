package backend.engine.basicCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class Neutral extends Instruction
{

    public Neutral(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        incrementProgramCounter(contextMap);
    }

    @Override
    public List<Command> expand(int level) {
        return List.of();
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public CommandType getType() {
        return CommandType.BASIC;
    }

    @Override
    public int getNumberOfArgs() {
        return 0;
    }

    @Override
    public String getDisplayFormat(int instructionNumber)
    {
        String commandPart = String.format("%s <- %s", mainVarName, mainVarName);
        return formatDisplay(instructionNumber, commandPart);
    }
}
