package engine.core;

import dto.engine.*;
import engine.exception.FunctionAlreadyExist;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SFunction;
import engine.generated_2.SProgram;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Engine {

    private final String programName;
    private final String mainProgramName;
    private final InstructionSequence instructionSequence;
    private final @NotNull FunctionManager functionManager;
    private @Nullable SFunction originalSFunction;
    private @Nullable String funcName;
    private float averageCycles = 0;
    private int numberOfExecutions = 0;

    private Engine(@NotNull SProgram program,
                   @NotNull Map<String, Engine> allFunctionAndProgramsInSystem)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {
        this.programName = this.mainProgramName = program.getName();
        // only for main program
        functionManager = FunctionManager.createForProgram(program, allFunctionAndProgramsInSystem,
                this, mainProgramName);
        instructionSequence = InstructionSequence.createFrom(program, functionManager);
        functionManager.finishInitialization();
        instructionSequence.finalizeInitialization();
    }

    private Engine(@NotNull SFunction function,
                   @NotNull String mainProgramName,
                   @NotNull FunctionManager functionManager) throws LabelNotExist {
        this.programName = function.getName();
        this.mainProgramName = mainProgramName;
        funcName = function.getUserString();

        instructionSequence = InstructionSequence.createFrom(function, functionManager);
        this.functionManager = functionManager;

        originalSFunction = function;
    }

    /**
     * Constructor for ProgramEngine. Initializes the program engine with the given SProgram
     * and a map of all functions in the server.
     *
     * @param program                        the SProgram to initialize the engine with
     * @param allFunctionAndProgramsInSystem a map of all functions and programs in the server
     * @throws LabelNotExist        if a label in the program does not exist
     * @throws FunctionNotFound     if a function in the program is not found in the server
     * @throws FunctionAlreadyExist if a function in the program already exists in the server
     */
    public static Engine createMainProgramEngine(@NotNull SProgram program,
                                                 @NotNull Map<String, Engine> allFunctionAndProgramsInSystem)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {
        return new Engine(program, allFunctionAndProgramsInSystem);
    }

    /**
     * Constructor for ProgramEngine. Initializes the program engine with the given SFunction
     * and the name of the main program.
     *
     * @param function        the SFunction to initialize the engine with
     * @param mainProgramName the name of the main program
     * @param functionManager the FunctionManager managing functions in the main program
     * @throws LabelNotExist if a label in the function does not exist
     */
    public static Engine createFunctionEngine(@NotNull SFunction function,
                                              @NotNull String mainProgramName,
                                              @NotNull FunctionManager functionManager)
            throws LabelNotExist {
        return new Engine(function, mainProgramName, functionManager);
    }

    public String getProgramName() {
        return programName;
    }

    public void addProgramAndFunctionsToSystem(@NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                                               @NotNull Map<String, Engine> functionsInSystem) {

        functionManager.addProgramAndFunctionsToSystem(allFunctionsAndProgramsInSystem,
                functionsInSystem);
    }


    public boolean isVariableInContext(String varName) {
        return instructionSequence.isVariableInContext(varName);
    }

    public ExecutionResultDTO run(int expandLevel, @NotNull Map<String, Integer> arguments) {
        List<Instruction> executedInstructions = instructionSequence.getInstructionsCopy(expandLevel);
        Map<String, Integer> executedMap = instructionSequence.getContextMapCopy(expandLevel);
        ProgramRunner runner = ProgramRunner.createFrom(executedInstructions, executedMap);
        ExecutionResultValuesDTO valuesResult = runner.run(expandLevel, arguments);
        averageCycles = calcAverageCycles(valuesResult.cycleCount());
        numberOfExecutions++;

        return ExecutionResultDTO.from(
                valuesResult, isMainProgram(), getRepresentationName(),
                instructionSequence.getMinimumArchitectureTypeNeededAtExpandLevel(expandLevel));
    }

    public ExecutionResultDTO run(@NotNull Map<String, Integer> arguments) {
        return run(0, arguments);
    }

    private float calcAverageCycles(int newCycles) {
        return (averageCycles * numberOfExecutions + newCycles) / numberOfExecutions + 1;
    }

    public @NotNull ProgramDebugger startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments) {
        ProgramDebugger debugger = ProgramDebugger.create(
                instructionSequence,
                expandLevel);
        debugger.startDebugSession(expandLevel, arguments);
        return debugger;
    }

    public @NotNull ProgramDTO getProgramByExpandLevelDTO(int expandLevel) {
        List<Instruction> instructionsAtLevel = instructionSequence.getInstructionsCopy(expandLevel);
        return new ProgramDTO(
                programName,
                instructionSequence.getSortedArgumentsNames(),
                instructionSequence.getLabels(expandLevel),
                java.util.stream.IntStream.range(0, instructionsAtLevel.size())
                        .mapToObj(i -> instructionsAtLevel.get(i).toDTO(i))
                        .toList()
        );
    }

    public @NotNull ProgramDTO getBasicProgramDTO() {
        return getProgramByExpandLevelDTO(0);
    }


    public @NotNull List<String> getSortedProgramArgsNames() {
        return new ArrayList<>(instructionSequence.getSortedArgumentsNames());
    }

    public @NotNull Set<String> getAllVariablesNames(int expandLevel, boolean includeLabels) {
        return instructionSequence.getAllVariablesNames(expandLevel, includeLabels);
    }

    /**
     * Get sorted arguments at a specific expansion level.
     * Used in debugging a program, because arguments can change during execution.
     *
     * @param expandLevel the expansion level to get the arguments from
     * @return a map of argument names to their values at the specified expansion level
     */
    public @NotNull Map<String, Integer> getSortedArgumentsMap(int expandLevel) {
        return instructionSequence.getSortedArgumentsMap(expandLevel);
    }

    public @NotNull Map<String, Integer> getSortedArgumentsMap() {
        return getSortedArgumentsMap(0);
    }

    public @NotNull Map<String, Integer> getSortedWorkVars(int expandLevel) {
        return instructionSequence.getSortedWorkVars(expandLevel);
    }

    public @Nullable String getFuncName() {
        return funcName;
    }

    // Enhanced debugger methods with proper state management


    @Contract(pure = true)
    protected @NotNull List<Instruction> getTempFunctionBasicInstructions() throws LabelNotExist {
        if (originalSFunction != null) {
            InstructionSequence temp = InstructionSequence.createFrom(originalSFunction, functionManager);
            return temp.getBasicInstructionsCopy();
        } else {
            throw new IllegalStateException("Not a function");
        }
    }

    public boolean isFunction() {
        return !isMainProgram();
    }

    public boolean isMainProgram() {
        return funcName == null;
    }

    public String getRepresentationName() {
        return isFunction() ? funcName : programName;
    }


    // TODO - implement user management and replace userFiller
    public @NotNull ProgramMetadata programToMetadata() {
        if (!isFunction()) {
            String userFiller = "remove_me"; // Placeholder until user management is implemented
            return new ProgramMetadata(programName, userFiller,
                    instructionSequence.getOriginalInstructionCount(), instructionSequence.getMaxExpandLevel(),
                    numberOfExecutions, averageCycles);
        } else {
            throw new IllegalStateException("Cannot get metadata for a function from a program");
        }
    }

    public @NotNull FunctionMetadata functionToMetadata() {
        if (isFunction()) {
            String userFiller = "remove_me"; // Placeholder until user management is implemented
            return new FunctionMetadata(programName, mainProgramName, userFiller,
                    instructionSequence.getOriginalInstructionCount(), instructionSequence.getMaxExpandLevel());
        } else {
            throw new IllegalStateException("Cannot get metadata for a program from a function");
        }
    }

    public int getMaxExpandLevel() {
        return instructionSequence.getMaxExpandLevel();
    }
}