package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.ProgramEngine;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Quote extends Instruction {
    private boolean isFinishedInitialization = false;
    public final static String functionNameArgumentName = "functionName";
    public final static String functionArgumentsArgumentName = "functionArguments";
    private final @NotNull ProgramEngine mainFunction;
    private ProgramEngine functionToRun;
    private String allArgsString;
    private List<String> funcArgsNames;
    private final List<Quote> subfunctionCalls = new ArrayList<>();
    private int subFunctionsCycles;

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

    public void initAndValidateQuote() {
        String funcName = args.get(functionNameArgumentName);
        allArgsString = args.get(functionArgumentsArgumentName) == null ? "" : args.get(functionArgumentsArgumentName);
        Map<String, ProgramEngine> functions = mainFunction.getAllFunctionsInMain();
        if (functions == null) {
            mainFunction.addToUninitializedQuotes(this);
            return;
        }

        if (!functions.containsKey(funcName) && !mainFunction.getProgramName().equals(funcName)) {
            throw new IllegalArgumentException("No such function: " + funcName);
        }

        isFinishedInitialization = true;
        functionToRun = funcName.equals(mainFunction.getProgramName()) ?
                mainFunction : functions.get(funcName);

        if (allArgsString.isBlank()) {
            funcArgsNames = List.of();
        } else {
            funcArgsNames = ProgramUtils.splitArgs(allArgsString);
        }

        initSubfunctionCalls();
    }

    private void initSubfunctionCalls() {
        subfunctionCalls.clear();
        for (String argName : funcArgsNames) {
            if (ProgramUtils.isFunctionCall(argName)) {
                Quote functionCall = Instruction.createQuoteFromString(argName, mainFunction);
                subfunctionCalls.add(functionCall);
            }
        }
        subFunctionsCycles = subfunctionCalls.stream()
                .mapToInt(Quote::getCycles)
                .sum();
    }

    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        if (isFinishedInitialization) {
            saveResult(contextMap, executeAndGetResult(contextMap));
            incrementProgramCounter(contextMap);
        }
    }

    public void saveResult(@NotNull Map<String, Integer> contextMap, int result) {
        contextMap.put(mainVarName, result);
    }

    public int executeAndGetResult(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        List<Integer> arguments = getArgumentsValues(contextMap);
        Map<String, Integer> functionToRunNeededArguments = functionToRun.getSortedArguments();
        prepareArguments(functionToRunNeededArguments, arguments);
        functionToRun.run(functionToRunNeededArguments);
        return functionToRun.getOutput();
    }

    private @NotNull List<Integer> getArgumentsValues(@NotNull Map<String, Integer> contextMap) {
        List<Integer> argumentsValues = new ArrayList<>();
        int quoteIndex = 0;
        for (String argName : funcArgsNames) {
            if (ProgramUtils.isFunctionCall(argName)) {
                Quote functionCall = subfunctionCalls.get(quoteIndex);
                argumentsValues.add(functionCall.executeAndGetResult(contextMap));
                quoteIndex++;
            } else {
                if (contextMap.containsKey(argName)) {
                    argumentsValues.add(contextMap.get(argName));
                } else {
                    throw new IllegalArgumentException("No such variable in context: " + argName);
                }
            }
        }
        return argumentsValues;
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
        if (isFinishedInitialization) {
            final int quoteOverhead = 5;
            return quoteOverhead + functionToRun.getTotalCycles() + subFunctionsCycles;
        }
        return 0; // not initialized yet
    }

    public int getFunctionCycles() {
        if (isFinishedInitialization) {
            return functionToRun.getTotalCycles() + subFunctionsCycles;
        }
        return 0; // not initialized yet
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
        StringBuilder quoteStringBuilder = new StringBuilder(String.format("%s <- (%s", mainVarName, functionToRun.getFuncName()));
        if (allArgsString.isBlank()) {
            quoteStringBuilder.append(")");
        } else {
            List<String> allArgs = ProgramUtils.splitArgs(allArgsString);
            int subFunctionIndex = 0;
            for (String arg : allArgs) {
                quoteStringBuilder.append(",");
                if (ProgramUtils.isFunctionCall(arg)) {
                    Quote subFunction = subfunctionCalls.get(subFunctionIndex);
                    quoteStringBuilder.append(subFunction.getFunctionStringRepresentation());
                    subFunctionIndex++;
                } else {
                    quoteStringBuilder.append(arg);
                }
            }
            quoteStringBuilder.append(")");
        }
        return quoteStringBuilder.toString();
    }

    public @NotNull String getFunctionStringRepresentation() {
        String argsNames = allArgsString.isEmpty() ? "" : "," + allArgsString;
        return String.format("(%s%s)", functionToRun.getFuncName(), argsNames);
    }
}
