package engine.core;

import engine.core.syntheticCommand.Quote;
import engine.exception.FunctionAlreadyExist;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SFunction;
import engine.generated_2.SProgram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages functions defined within a program.
 * Responsible for building function engines, checking for name conflicts,
 * and initializing quotes that reference functions.
 */
public class FunctionManager {
    private final @NotNull Engine mainProgramEngine;
    private final @NotNull String mainProgramName;
    private final @NotNull Map<String, Engine> allFunctionsAndProgramsInSystem;
    private final @NotNull List<Quote> uninitializedQuotes = new ArrayList<>();
    private final @NotNull Map<String, Engine> functionsInCurrentProgram;
    private boolean initialised = false;

    private FunctionManager(@NotNull SProgram sMainProgram,
                            @NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                            @NotNull Engine mainProgramEngine,
                            @NotNull String mainProgramName)
            throws FunctionAlreadyExist, LabelNotExist {

        this.functionsInCurrentProgram = buildFunctions(sMainProgram, mainProgramName,
                mainProgramEngine);
        this.allFunctionsAndProgramsInSystem = allFunctionsAndProgramsInSystem;
        this.mainProgramName = mainProgramName;
        this.mainProgramEngine = mainProgramEngine;
        checkForNameConflicts(allFunctionsAndProgramsInSystem);

    }

    /**
     * Create a FunctionManager for the given SProgram.
     * create manger for the main program
     * In charge of building and managing functions defined in the program.
     *
     * @param sProgram                        the SProgram containing function definitions
     * @param allFunctionsAndProgramsInSystem a map of all functions and programs in the server
     * @param mainProgramEngine               the ProgramEngine of the main program
     * @param mainProgramName                 the name of the main program
     * @return a FunctionManager instance for the given SProgram
     * @throws LabelNotExist        if a label referenced in an instruction does not exist
     * @throws FunctionAlreadyExist if there is a name conflict with functions or programs in the server
     */
    public static @NotNull FunctionManager createForProgram(@NotNull SProgram sProgram,
                                                            @NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                                                            @NotNull Engine mainProgramEngine,
                                                            @NotNull String mainProgramName)
            throws LabelNotExist, FunctionAlreadyExist {

        return new FunctionManager(sProgram, allFunctionsAndProgramsInSystem, mainProgramEngine,
                mainProgramName);

    }

    public void finishInitialization() throws FunctionNotFound {
        finishInitQuotes();
        initialised = true;
        for (Engine functionEngine : functionsInCurrentProgram.values()) {
            functionEngine.finishInitialization();
        }
    }

    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Build the functions defined in the given SProgram.
     *
     * @param program           the SProgram containing function definitions
     * @param mainProgramName   the name of the main program
     * @param mainProgramEngine the ProgramEngine of the main program
     * @return a map of function names to their corresponding ProgramEngine instances
     * @throws LabelNotExist    if a label referenced in a function does not exist
     */

    private @NotNull Map<String, Engine> buildFunctions(@NotNull SProgram program,
                                                        @NotNull String mainProgramName,
                                                        @NotNull Engine mainProgramEngine)
            throws LabelNotExist {
        Map<String, Engine> functionMap = new HashMap<>();
        List<SFunction> sFunctions = program.getSFunctions().getSFunction();
        String userUploadedBy = mainProgramEngine.getUserUploadedBy();

        for (SFunction sFunc : sFunctions) {
            Engine engine = Engine.createFunctionEngine(sFunc, mainProgramName, this, userUploadedBy);
            functionMap.put(engine.getProgramName(), engine);
        }

        return functionMap;
    }

    /**
     * Add a quote that needs to be initialized after all functions are set.
     *
     * @param quote the quote to be added
     */
    public void addToUninitializedQuotes(Quote quote) {
        uninitializedQuotes.add(quote);
    }

    private void finishInitQuotes() throws FunctionNotFound {
        for (Quote quote : uninitializedQuotes) {
            quote.validateAndFinishInit();
        }
        uninitializedQuotes.clear();
    }

    public void checkForNameConflicts(@NotNull Map<String, Engine> allFunctionAndProgramsInSystem)
            throws FunctionAlreadyExist {

        for (String functionNameInSystem : allFunctionAndProgramsInSystem.keySet()) {
            // Prevent having functions with the same name as the main program and vice versa
            if (functionsInCurrentProgram.containsKey(functionNameInSystem) ||
                    functionNameInSystem.equals(mainProgramName)) {
                throw new FunctionAlreadyExist(mainProgramName, functionNameInSystem);
            }
        }
    }

    public void addProgramAndFunctionsToSystem(@NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                                               @NotNull Map<String, Engine> functionsInSystem) {
        allFunctionsAndProgramsInSystem.put(mainProgramName, mainProgramEngine);
        allFunctionsAndProgramsInSystem.putAll(functionsInCurrentProgram);
        functionsInSystem.putAll(functionsInCurrentProgram);
    }

    /**
     * Get a function by its name. First checks in current program's functions,
     * then in the server-wide functions.
     * if not found, returns null.
     *
     * @param programName the name of the function to retrieve
     * @return the ProgramEngine of the function, or null if not found
     */
    public @Nullable Engine getFunction(@NotNull String programName) {
        return functionsInCurrentProgram.getOrDefault(programName,
                allFunctionsAndProgramsInSystem.get(programName));
    }

    public int getFunctionCount() {
        return functionsInCurrentProgram.size();
    }
}
