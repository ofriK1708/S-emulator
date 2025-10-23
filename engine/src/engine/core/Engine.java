package engine.core;

import dto.engine.*;
import engine.exception.*;
import engine.generated_2.SFunction;
import engine.generated_2.SProgram;
import engine.utils.ArchitectureType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * The Engine class represents a program engine that can execute SPrograms and SFunctions.
 * It manages the instruction sequence, function management, and execution of the program or function.
 * <p>
 * It provides methods to run the program/function, start debugging sessions, and retrieve program metadata.
 * </p>
 */
public class Engine {

    private final String programName;
    private final @NotNull String userUploadedBy;
    private final String mainProgramName;
    private final @NotNull InstructionSequence instructionSequence;
    private final @NotNull FunctionManager functionManager;
    private @Nullable SFunction originalSFunction;
    private @Nullable String funcName;
    private float averageCreditsCost = 0;
    private int numberOfExecutions = 0;

    private Engine(@NotNull SProgram program,
                   @NotNull Map<String, Engine> allFunctionAndProgramsInSystem,
                   @NotNull String uploadedBy)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {
        this.programName = this.mainProgramName = program.getName();
        this.userUploadedBy = uploadedBy;
        // only for main program
        functionManager = FunctionManager.createForProgram(program, allFunctionAndProgramsInSystem,
                this, mainProgramName);
        instructionSequence = InstructionSequence.createFrom(program, functionManager);
        functionManager.finishInitialization();
        instructionSequence.finalizeInitialization();
    }

    private Engine(@NotNull SFunction function,
                   @NotNull String mainProgramName,
                   @NotNull FunctionManager functionManager,
                   @NotNull String uploadedBy) throws LabelNotExist {
        this.programName = function.getName();
        this.userUploadedBy = uploadedBy;
        this.mainProgramName = mainProgramName;
        funcName = function.getUserString();
        originalSFunction = function;
        this.functionManager = functionManager;
        instructionSequence = InstructionSequence.createFrom(function, functionManager);


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
    public static @NotNull Engine createMainProgramEngine(@NotNull SProgram program,
                                                          @NotNull Map<String, Engine> allFunctionAndProgramsInSystem,
                                                          @NotNull String uploadedBy)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {
        return new Engine(program, allFunctionAndProgramsInSystem, uploadedBy);
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
    public static @NotNull Engine createFunctionEngine(@NotNull SFunction function,
                                                       @NotNull String mainProgramName,
                                                       @NotNull FunctionManager functionManager,
                                                       @NotNull String uploadedBy)
            throws LabelNotExist {
        return new Engine(function, mainProgramName, functionManager, uploadedBy);
    }

    public void finishInitialization() {
        instructionSequence.finalizeInitialization();
    }

    public String getProgramName() {
        return programName;
    }

    public void addProgramAndFunctionsToSystem(@NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                                               @NotNull Map<String, Engine> functionsInSystem) {

        functionManager.addProgramAndFunctionsToSystem(allFunctionsAndProgramsInSystem,
                functionsInSystem);
    }

    public int getFunctionsCount() {
        return functionManager.getFunctionCount();
    }


    public boolean isVariableInContext(String varName) {
        return instructionSequence.isVariableInContext(varName);
    }

    public @NotNull FullExecutionResultDTO run(int expandLevel, @NotNull Map<String, Integer> arguments,
                                               int userCredits,
                                               @NotNull ArchitectureType architectureType)
            throws ExpandLevelOutOfBounds, IllegalArchitectureType, InsufficientCredits {
        validateRunPossibility(expandLevel, userCredits, architectureType);
        ProgramExecutable executable = instructionSequence.getProgramExecutableAtExpandLevel(expandLevel);
        ProgramRunner runner = ProgramRunner.createFrom(executable, userCredits);
        ExecutionResultValuesDTO valuesResult = runner.run(expandLevel, arguments);
        averageCreditsCost = calcAverageCredits(valuesResult.creditsCost());
        numberOfExecutions++;

        return FullExecutionResultDTO.from(
                valuesResult, isMainProgram(), getRepresentationName(),
                architectureType);
    }

    private void validateRunPossibility(int expandLevel, int userCredits,
                                        @NotNull ArchitectureType architectureType)
            throws ExpandLevelOutOfBounds, IllegalArchitectureType, InsufficientCredits {
        if (expandLevel < 0 || expandLevel > instructionSequence.getMaxExpandLevel()) {
            throw new ExpandLevelOutOfBounds("Expanding level out of bounds: ", expandLevel, 0,
                    instructionSequence.getMaxExpandLevel());
        }

        ArchitectureType requiredArchitecture = instructionSequence.
                getMinimumArchitectureTypeNeededAtExpandLevel(expandLevel);
        if (architectureType.compareTo(requiredArchitecture) < 0) {
            throw new IllegalArchitectureType("Architecture type not sufficient for execution",
                    requiredArchitecture, architectureType);
        }

        int requiredCredits = requiredArchitecture.getCreditsCost();
        if (userCredits < requiredCredits) {
            throw new InsufficientCredits("Not enough credits to run the program/function at the given architecture " +
                    "type (" + architectureType + ")",
                    requiredCredits, userCredits);
        }
    }

    /**
     * this run happen internally, without user credits limitation and at expand level 0
     *
     * @param arguments a map of argument names to their integer values
     * @return an ExecutionResultDTO containing the results of the execution
     */
    public FullExecutionResultDTO run(@NotNull Map<String, Integer> arguments) {
        return run(0, arguments, Integer.MAX_VALUE, ArchitectureType.INNER_RUN_ARCHITECTURE);
    }

    private float calcAverageCredits(int latestRunCreditsCost) {
        return (averageCreditsCost * numberOfExecutions + latestRunCreditsCost) / numberOfExecutions + 1;
    }

    public @NotNull ProgramDebugger startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments,
                                                      int userCredits, @NotNull ArchitectureType architectureType)
            throws ExpandLevelOutOfBounds, IllegalArchitectureType, InsufficientCredits {
        validateRunPossibility(expandLevel, userCredits, architectureType);
        userCredits -= architectureType.getCreditsCost();
        ProgramExecutable executable = instructionSequence.getProgramExecutableAtExpandLevel(expandLevel);
        ProgramDebugger debugger = ProgramDebugger.builder(executable, userCredits, expandLevel)
                .metadata(isMainProgram(), getRepresentationName(), architectureType)
                .build();
        return debugger.start(arguments);
    }

    public @NotNull ProgramDTO getProgramByExpandLevelDTO(int expandLevel) {
        List<Instruction> instructionsAtLevel = instructionSequence.getInstructionsCopy(expandLevel);
        return new ProgramDTO(
                programName,
                getSortedArgumentsMap(),
                instructionSequence.getMaxExpandLevel(),
                IntStream.range(0, instructionsAtLevel.size())
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
     * Get arguments (x1,x2,...)  sorted by their numeric suffix at a specific expansion level.
     * Used in debugging a program, because arguments can change during execution.
     *
     * @param expandLevel the expansion level to get the arguments from
     * @return a map of argument names to their values at the specified expansion level
     */
    public @NotNull Map<String, Integer> getSortedArgumentsMap(int expandLevel) {
        return instructionSequence.getSortedArgumentsMap(expandLevel);
    }

    /**
     * Get arguments (x1,x2,...)  sorted by their numeric suffix at expansion level 0.
     * Used to get arguments before running the program.
     *
     * @return a map of argument names to their values at expansion level 0
     */
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


    public @NotNull String getRepresentationName() {
        //noinspection DataFlowIssue
        return isFunction() ? funcName : programName;
    }

    public @NotNull ProgramMetadata programToMetadata() {
        if (!isFunction()) {
            return new ProgramMetadata(programName, userUploadedBy,
                    instructionSequence.getOriginalInstructionCount(), instructionSequence.getMaxExpandLevel(),
                    numberOfExecutions, averageCreditsCost);
        } else {
            throw new IllegalStateException("Cannot get metadata for a function from a program");
        }
    }

    public @NotNull FunctionMetadata functionToMetadata() {
        if (isFunction()) {
            return new FunctionMetadata(programName, mainProgramName, userUploadedBy,
                    instructionSequence.getOriginalInstructionCount(), instructionSequence.getMaxExpandLevel());
        } else {
            throw new IllegalStateException("Cannot get metadata for a program from a function");
        }
    }

    public @NotNull String getUserUploadedBy() {
        return userUploadedBy;
    }

    public int getMaxExpandLevel() {
        return instructionSequence.getMaxExpandLevel();
    }
}