package engine.core.syntheticCommand;

import dto.engine.ExecutionResultDTO;
import engine.core.Engine;
import engine.core.FunctionManager;
import engine.core.Instruction;
import engine.core.basicCommand.Neutral;
import engine.exception.FunctionNotFound;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Quote extends Instruction {
    public static final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_IV;
    public static final int ARCHITECTURE_CREDITS_COST = ARCHITECTURE_TYPE.getCreditsCost();
    private int quoteIndex = -1;
    private boolean isFinishedInitialization = false;
    public final static String functionNameArgumentName = "functionName";
    public final static String functionArgumentsArgumentName = "functionArguments";
    private final @NotNull FunctionManager functionManager;
    private Engine functionToRun;
    private String allArgsString;
    private List<String> funcArgsNames;
    private final List<Quote> subfunctionCalls = new ArrayList<>();
    private int executedCycles = 0;

    private Quote(@NotNull String mainVarName, @NotNull Map<String, String> args,
                  @NotNull String label, @NotNull FunctionManager functionManager,
                  int quoteIndex, boolean isFinishedInitialization) throws FunctionNotFound {
        super(mainVarName, args, label);
        this.functionManager = functionManager;
        this.quoteIndex = quoteIndex;
        if (!isFinishedInitialization) {
            functionManager.addToUninitializedQuotes(this);
        } else {
            validateAndFinishInit();
        }
    }

    /**
     * Copy constructor for Quote.
     * used when expanding a quote - to create a new quote based on the original one
     *
     * @param mainVarName      the main variable name to store the result
     * @param label            the label for the quote
     * @param quote            the original quote to copy from
     * @param derivedFrom      the instruction from which this quote is derived
     * @param derivedFromIndex the index of the instruction from which this quote is derived
     */
    public Quote(String mainVarName, String label, @NotNull Quote quote, @NotNull Instruction derivedFrom,
                 int derivedFromIndex) {
        super(mainVarName, quote.args, label, derivedFrom, derivedFromIndex);
        this.functionManager = quote.functionManager;
        this.functionToRun = quote.functionToRun;
        this.allArgsString = quote.allArgsString;
        this.funcArgsNames = new ArrayList<>(quote.funcArgsNames);
        this.subfunctionCalls.addAll(quote.subfunctionCalls);
        this.isFinishedInitialization = quote.isFinishedInitialization;
    }

    /**
     * Factory method to create an initial Quote.
     * used when creating a new Quote - every time an instructions sequence is created
     *
     * @param mainVarName     the main variable name to store the result
     * @param args            the arguments map for the quote
     * @param label           the label for the quote
     * @param functionManager the function manager to retrieve functions
     * @param quoteIndex      the index of the quote in the program
     * @return the created Quote instance
     */
    public static Quote createInitialQuote(@NotNull String mainVarName, @NotNull Map<String, String> args,
                                           @NotNull String label, @NotNull FunctionManager functionManager,
                                           int quoteIndex) {
        try {
            return new Quote(mainVarName, args, label, functionManager, quoteIndex, functionManager.isInitialised());
        } catch (FunctionNotFound ignored) {
            // this should not happen here, as we are not initializing yet
            throw new RuntimeException("Unexpected FunctionNotFound during initial Quote creation");
        }
    }

    /**
     * Factory method to create a sub-function Quote.
     * used when creating a Quote for a sub-function call within another Quote
     * this happens after initialization
     *
     * @param mainVarName     the main variable name to store the result
     * @param args            the arguments map for the quote
     * @param label           the label for the quote
     * @param functionManager the function manager to retrieve functions
     * @param quoteIndex      the index of the quote in the program
     * @return the created Quote instance
     * @throws FunctionNotFound if the function to run is not found
     */
    public static Quote createSubFunctionQuote(@NotNull String mainVarName, @NotNull Map<String, String> args,
                                               @NotNull String label, @NotNull FunctionManager functionManager,
                                               int quoteIndex) throws FunctionNotFound {
        return new Quote(mainVarName, args, label, functionManager, quoteIndex, true);
    }

    public int getExecutedCycles() {
        return executedCycles;
    }

    public void validateAndFinishInit() throws FunctionNotFound {
        String funcName = args.get(functionNameArgumentName);
        allArgsString = args.get(functionArgumentsArgumentName) == null ? "" : args.get(functionArgumentsArgumentName);
        functionToRun = functionManager.getFunction(funcName);

        if (functionToRun == null) {
            throw new FunctionNotFound(quoteIndex, getStringRepresentation(), functionManager.getMainProgramName(),
                    funcName);
        }
        isFinishedInitialization = true;
        if (allArgsString.isBlank()) {
            funcArgsNames = List.of();
        } else {
            funcArgsNames = ProgramUtils.splitArgs(allArgsString);
        }

        initSubfunctionCalls();
    }

    private void initSubfunctionCalls() throws FunctionNotFound {
        subfunctionCalls.clear();
        for (String argName : funcArgsNames) {
            if (ProgramUtils.isFunctionCall(argName)) {
                Quote functionCall = Instruction.createSubFunctionCall(argName, functionManager, quoteIndex);
                subfunctionCalls.add(functionCall);
            }
        }
    }

    private int calcSubFunctionCycles() {
        return subfunctionCalls.stream()
                .mapToInt(Quote::getExecutedCycles)
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
        Map<String, Integer> functionToRunNeededArguments = functionToRun.getSortedArgumentsMap();
        prepareArguments(functionToRunNeededArguments, arguments);
        ExecutionResultDTO result = functionToRun.run(functionToRunNeededArguments);
        executedCycles = result.cycleCount();
        return result.output();
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
            return quoteOverhead + executedCycles + calcSubFunctionCycles();
        }
        return 0; // not initialized yet
    }

    public int getFunctionCycles() {
        if (isFinishedInitialization) {
            return executedCycles + calcSubFunctionCycles();
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
    public @NotNull List<Instruction> expand(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex) {
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

    private void handelNewAssignment(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex,
                                     @NotNull Map<String, String> argsReplacements,
                                     @NotNull List<Instruction> expandedInstructions) {

        List<String> functionParamNames = functionToRun.getSortedProgramArgsNames();
        Iterator<Quote> subFuncIter = subfunctionCalls.iterator();
        Iterator<String> funcArgsName = funcArgsNames.iterator();
        for (String funcParam : functionParamNames) {
            if (funcArgsName.hasNext()) {
                String argName = funcArgsName.next();
                String newArgName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
                argsReplacements.put(funcParam, newArgName);
                if (ProgramUtils.isFunctionCall(argName)) {
                    Quote subFunc = subFuncIter.next();
                    expandedInstructions.add(new Quote(newArgName, "", subFunc, this, originalInstructionIndex));
                } else {
                    expandedInstructions.add(new Assignment(newArgName, Map.of(Assignment.sourceArgumentName,
                            argName), "", this, originalInstructionIndex));
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

    public void updateArguments(@NotNull String updatedArguments) {
        // only update if there is a change
        if (!allArgsString.equals(updatedArguments)) {
            this.allArgsString = updatedArguments;
            if (updatedArguments.isBlank()) {
                funcArgsNames = List.of();
            } else {
                funcArgsNames = ProgramUtils.splitArgs(updatedArguments);
            }
            try {
                initSubfunctionCalls();
            } catch (FunctionNotFound ignored) {
                // this should not happen as the functions are already validated
            }
        }
    }
}
