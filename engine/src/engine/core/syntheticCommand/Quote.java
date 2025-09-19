package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.ProgramEngine;
import engine.utils.CommandType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Quote extends Instruction {
    private final static String functionNameArgumentName = "functionName";
    private final static String functionArgumentsArgumentName = "functionArgumentsArgumentName";
    private final @NotNull ProgramEngine mainFunction;
    private ProgramEngine functionToRun;
    private String allArgsString;
    private List<String> funcArgsNames;

    public Quote(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex, @NotNull ProgramEngine mainFunction) {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        this.mainFunction = mainFunction;
        initAndValidateQuote();
    }

    public Quote(String mainVarName, Map<String, String> args, String label, @NotNull ProgramEngine mainFunction) {
        super(mainVarName, args, label);
        this.mainFunction = mainFunction;
        initAndValidateQuote();
    }

    private void initAndValidateQuote() {
        String funcName = args.get(functionNameArgumentName);
        allArgsString = args.get(functionArgumentsArgumentName) == null ? "" : args.get(functionArgumentsArgumentName);
        Map<String, ProgramEngine> functions = mainFunction.getFunctions();
        funcArgsNames = allArgsString.isBlank() ? List.of() : List.of(allArgsString.split(","));

        if (!functions.containsKey(funcName) && !mainFunction.getProgramName().equals(funcName)) {
            throw new IllegalArgumentException("No such function: " + funcName); // TODO: custom exception
        }
        functionToRun = funcName.equals(mainFunction.getProgramName()) ?
                mainFunction : functions.get(funcName);
    }

    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        saveResult(contextMap, executeAndGetResult(contextMap));
        incrementProgramCounter(contextMap);
    }

    public void saveResult(@NotNull Map<String, Integer> contextMap, int result) {
        contextMap.put(mainVarName, result);
    }

    public int executeAndGetResult(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        List<Integer> arguments = getArgumentsValues(contextMap, funcArgsNames);
        Map<String, Integer> functionToRunNeededArguments = functionToRun.getSortedArguments();
        prepareArguments(functionToRunNeededArguments, arguments);
        functionToRun.run(functionToRunNeededArguments);
        return functionToRun.getOutput();
    }

    private @NotNull List<Integer> getArgumentsValues(@NotNull Map<String, Integer> contextMap, @NotNull List<String> funcArgsName) {
        return funcArgsName.stream()
                .map(String::trim)
                .map(contextMap::get)
                .toList();
    }

    private void prepareArguments(@NotNull Map<String, Integer> functionToRunArguments, @NotNull List<Integer> arguments) {
        int i = 0;
        for (String argName : functionToRunArguments.keySet()) {
            if (i < arguments.size()) {
                functionToRunArguments.put(argName, arguments.get(i));
                i++;
            } else {
                functionToRunArguments.put(argName, 0); // default value for missing arguments
            }
        }
    }

    @Override
    public int getCycles() {
        final int quoteOverhead = 5;
        return quoteOverhead + functionToRun.getTotalCycles();
    }

    public int getFunctionCycles() {
        return functionToRun.getTotalCycles();
    }

    @Override
    public @NotNull CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getExpandLevel() {
        return 0; //return ProgramUtils.calculateExpandedLevel(this); TODO - implement this after implementing expansion
    }

    @Override
    public @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex) {
        return List.of(); // TODO - implement this
    }

    @Override
    public @NotNull String getStringRepresentation() {
        String argsNames = allArgsString.isEmpty() ? "" : "," + allArgsString;
        return String.format("%s <- (%s%s)", mainVarName, functionToRun.getFuncName(), argsNames);
    }

    public @NotNull String getFunctionStringRepresentation() {
        String argsNames = allArgsString.isEmpty() ? "" : "," + allArgsString;
        return String.format("(%s%s)", functionToRun.getFuncName(), argsNames);
    }
}
