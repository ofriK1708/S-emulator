package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.ProgramEngine;
import engine.core.basicCommand.Neutral;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

    public Quote(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom,
                 int derivedFromIndex, @NotNull ProgramEngine mainFunction) {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        this.mainFunction = mainFunction;
        initAndValidateQuote();
    }

    public Quote(String mainVarName, Map<String, String> args, String label, @NotNull ProgramEngine mainFunction) {
        super(mainVarName, args, label);
        this.mainFunction = mainFunction;

        initAndValidateQuote();
    }

    public Quote(String mainVarName, String label, Quote quote, Instruction derivedFrom, int derivedFromIndex) {
        super(mainVarName, quote.args, label, derivedFrom, derivedFromIndex);
        this.mainFunction = quote.mainFunction;
        this.functionToRun = quote.functionToRun;
        this.allArgsString = quote.allArgsString;
        this.funcArgsNames = new ArrayList<>(quote.funcArgsNames);
        this.subfunctionCalls.addAll(quote.subfunctionCalls);
        this.subFunctionsCycles = quote.subFunctionsCycles;
        this.isFinishedInitialization = quote.isFinishedInitialization;
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
        calcSubFunctionCycles();
    }

    private void calcSubFunctionCycles() {
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

    private void prepareArguments(@NotNull Map<String, Integer> functionToRunArguments,
                                  @NotNull List<Integer> arguments) {
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
            return quoteOverhead + functionToRun.getTotalCycles();
        }
        return 0; // not initialized yet
    }

    public int getFunctionCycles() {
        if (isFinishedInitialization) {
            return functionToRun.getTotalCycles();
        }
        return 0; // not initialized yet
    }

    @Override
    public @NotNull CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getExpandLevel() {
        return ProgramUtils.calculateExpandedLevel(this);
    }

    @Override
    public @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex) {
        List<Instruction> expandedInstructions = new ArrayList<>();
        Map<String, String> argsReplacements = new HashMap<>();
        if (!label.isBlank()) {
            expandedInstructions.add(new Neutral(ProgramUtils.OUTPUT_NAME, Map.of(), label, this,
                    originalInstructionIndex));
        }
        handelNewAssignment(contextMap, originalInstructionIndex, argsReplacements, expandedInstructions);
        expandedInstructions.addAll(getUpdatedFunctionInstructions(contextMap, functionToRun, mainVarName,
                argsReplacements, this, originalInstructionIndex));
        return expandedInstructions;
    }

    private void handelNewAssignment(Map<String, Integer> contextMap, int originalInstructionIndex,
                                     Map<String, String> argsReplacements, List<Instruction> expandedInstructions) {

        List<String> functionParamNames = functionToRun.getSortedProgramArgsNames();
        Iterator<Quote> subFuncIter = subfunctionCalls.iterator();
        Iterator<String> funcArgsName = funcArgsNames.iterator();
        for (String funcParam : functionParamNames) {
            if (funcArgsName.hasNext()) {
                String argName = funcArgsName.next();
                if (ProgramUtils.isArgument(argName)) {
                    argsReplacements.put(funcParam, argName);
                } else {
                    String newArgName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
                    argsReplacements.put(funcParam, newArgName);
                    if (ProgramUtils.isFunctionCall(argName)) {
                        Quote subFunc = subFuncIter.next();
                        expandedInstructions.add(new Quote(newArgName, "", subFunc, this, originalInstructionIndex));
                    } else {
                        expandedInstructions.add(new Assignment(newArgName, Map.of(Assignment.sourceArgumentName,
                                argName), "", this, originalInstructionIndex));
                    }
                }
            } else {
                String newArgName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
                argsReplacements.put(funcParam, newArgName);
                expandedInstructions.add(new ZeroVariable(newArgName, Map.of(), "", this, originalInstructionIndex));
            }
        }
    }

    @Override
    public @NotNull String getStringRepresentation() {
        return String.format("%s <- %s", mainVarName,
                getFunctionStringRepresentation());
    }

    public @NotNull String getFunctionStringRepresentation() {
        StringBuilder quoteStringBuilder = new StringBuilder();
        quoteStringBuilder.append("(").append(functionToRun.getFuncName());

        if (!allArgsString.isEmpty()) {
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
        }

        quoteStringBuilder.append(")");
        return quoteStringBuilder.toString();
    }

    public void updateArguments(String updatedArguments) {
        // only update if there is a change
        if (!allArgsString.equals(updatedArguments)) {
            this.allArgsString = updatedArguments;
            if (updatedArguments.isBlank()) {
                funcArgsNames = List.of();
            } else {
                funcArgsNames = ProgramUtils.splitArgs(updatedArguments);
            }
            initSubfunctionCalls();
        }
    }
}
