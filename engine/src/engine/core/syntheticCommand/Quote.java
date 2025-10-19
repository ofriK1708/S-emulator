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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Quote extends Instruction {
    // region Fields
    public final static @NotNull String functionNameArgumentName = "functionName";
    private static final @NotNull ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_IV;
    private static final int ARCHITECTURE_CREDITS_COST = ARCHITECTURE_TYPE.getCreditsCost();
    public final static @NotNull String functionArgumentsArgumentName = "functionArguments";
    private final @NotNull String enclosingFunctionName;
    private int quoteIndex = -1;
    private boolean isFinishedInitialization = false;
    private final @NotNull FunctionManager functionManager;
    private @Nullable Engine functionToRun;
    private String allArgsString;
    private List<String> funcArgsNames;
    private final List<Quote> subfunctionCalls = new ArrayList<>();
    private int executedCycles = 0;
    // endregion

    // region Constructors
    private Quote(@NotNull String mainVarName, @NotNull Map<String, String> args,
                  @NotNull String label, @NotNull FunctionManager functionManager,
                  int quoteIndex, @NotNull String enclosingFunctionName, boolean isFinishedInitialization) throws FunctionNotFound {
        super(mainVarName, args, label);
        this.functionManager = functionManager;
        this.quoteIndex = quoteIndex;
        this.enclosingFunctionName = enclosingFunctionName;
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
        this.enclosingFunctionName = quote.enclosingFunctionName;
    }
    // endregion

    // region Factory methods

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
    public static @NotNull Quote createInitialQuote(@NotNull String mainVarName, @NotNull Map<String, String> args,
                                                    @NotNull String label, @NotNull FunctionManager functionManager,
                                                    int quoteIndex, @NotNull String enclosingFunctionName) {
        try {
            return new Quote(mainVarName, args, label, functionManager, quoteIndex, enclosingFunctionName,
                    functionManager.isInitialised());
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
    public static @NotNull Quote createSubFunctionQuote(@NotNull String mainVarName, @NotNull Map<String, String> args,
                                                        @NotNull String label, @NotNull FunctionManager functionManager,
                                                        int quoteIndex, @NotNull String enclosingFunctionName) throws FunctionNotFound {
        return new Quote(mainVarName, args, label, functionManager, quoteIndex, enclosingFunctionName, true);
    }
    // endregion

    // region Initialization
    public void validateAndFinishInit() throws FunctionNotFound {
        String funcName = args.get(functionNameArgumentName);
        allArgsString = args.get(functionArgumentsArgumentName) == null ? "" : args.get(functionArgumentsArgumentName);
        functionToRun = functionManager.getFunction(funcName);

        if (functionToRun == null) {
            throw new FunctionNotFound(quoteIndex, this.getClass().getSimpleName(),
                    enclosingFunctionName,
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
                Quote functionCall = Instruction.createSubFunctionCall(argName, functionManager, quoteIndex,
                        enclosingFunctionName);
                subfunctionCalls.add(functionCall);
            }
        }
    }
    // endregion

    // region Execution
    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        if (isFinishedInitialization) {
            saveResult(contextMap, executeAndGetResult(contextMap));
            incrementProgramCounter(contextMap);
        }
    }

    public int executeAndGetResult(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        List<Integer> arguments = getArgumentsValues(contextMap);
        Map<String, Integer> functionToRunNeededArguments = functionToRun.getSortedArgumentsMap();
        prepareArguments(functionToRunNeededArguments, arguments);
        ExecutionResultDTO result = functionToRun.run(functionToRunNeededArguments);
        executedCycles = result.cycleCount();
        return result.output();
    }

    public void saveResult(@NotNull Map<String, Integer> contextMap, int result) {
        contextMap.put(mainVarName, result);
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
    // endregion

    // region Expansion
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
    // endregion

    // region Getters & Setters
    @Override
    public int getArchitectureCreditsCost() {
        return ARCHITECTURE_CREDITS_COST;
    }

    @Override
    public @NotNull ArchitectureType getArchitectureType() {
        return ARCHITECTURE_TYPE;
    }

    public int getExecutedCycles() {
        return executedCycles;
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
    // endregion

    // region Private methods
    private int calcSubFunctionCycles() {
        return subfunctionCalls.stream()
                .mapToInt(Quote::getExecutedCycles)
                .sum();
    }
    // endregion
}
