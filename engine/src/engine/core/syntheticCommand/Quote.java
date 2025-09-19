package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.ProgramEngine;
import engine.utils.CommandType;

import java.util.List;
import java.util.Map;

public class Quote extends Instruction {
    private final ProgramEngine mainFunction;
    private ProgramEngine functionToRun;
    private String allArgsString;
    private Map<String, ProgramEngine> functions;
    private List<String> funcArgsNames;

    public Quote(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex, ProgramEngine mainFunction) {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        this.mainFunction = mainFunction;
        initAndValidateQuote();
    }

    public Quote(String mainVarName, Map<String, String> args, String label, ProgramEngine mainFunction) {
        super(mainVarName, args, label);
        this.mainFunction = mainFunction;
        initAndValidateQuote();
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException {

        List<Integer> arguments = getArgumentsValues(contextMap, funcArgsNames);
        Map<String, Integer> functionToRunArguments = functionToRun.getArguments();
        validateArgumentsForFunctionToRun(arguments, functionToRunArguments);
        prepareArguments(functionToRunArguments, arguments);
        functionToRun.run(functionToRunArguments);
        contextMap.put(mainVarName, functionToRun.getOutput());
        incrementProgramCounter(contextMap);
    }

    private List<Integer> getArgumentsValues(Map<String, Integer> contextMap, List<String> funcArgsName) {
        return funcArgsName.stream()
                .map(String::trim)
                .map(contextMap::get)
                .toList();
    }

    private void prepareArguments(Map<String, Integer> functionToRunArguments, List<Integer> arguments) {
        int i = 0;
        for (String argName : functionToRunArguments.keySet()) {
            functionToRunArguments.put(argName, arguments.get(i));
            i++;
        }
    }

    // TODO - change this to padding in zeroes if needed
    private void validateArgumentsForFunctionToRun(List<Integer> arguments, Map<String, Integer> functionToRunArguments) {
        if (arguments.size() != functionToRunArguments.size()) {
            throw new IllegalArgumentException("Number of arguments does not match number of program arguments");
        }
    }

    private void initAndValidateQuote() {
        String funcName = args.get("functionName");
        allArgsString = args.get("functionArguments");
        functions = mainFunction.getFunctions();
        funcArgsNames = allArgsString.isBlank() ? List.of() : List.of(allArgsString.split(","));

        if (!functions.containsKey(funcName) && !mainFunction.getProgramName().equals(funcName)) {
            throw new IllegalArgumentException("No such function: " + funcName); // TODO: custom exception
        }
        functionToRun = funcName.equals(mainFunction.getProgramName()) ?
                mainFunction : functions.get(funcName);
    }

    @Override
    public int getCycles() {
        final int quoteOverhead = 5;
        return quoteOverhead + functionToRun.getTotalCycles();
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getExpandLevel() {
        return 0;
        //return ProgramUtils.calculateExpandedLevel(this); TODO - implement this after implementing expansion
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex) {
        return List.of();
    }

    @Override
    public String getStringRepresentation() {
        String argsNames = allArgsString.isEmpty() ? "" : "," + allArgsString;
        return String.format("%s <- (%s%s)", mainVarName, functionToRun.getFuncName(), argsNames);
    }
}
