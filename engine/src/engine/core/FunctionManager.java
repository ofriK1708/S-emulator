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
            throws FunctionNotFound, FunctionAlreadyExist, LabelNotExist {

        this.functionsInCurrentProgram = buildFunctions(sMainProgram, mainProgramName,
                mainProgramEngine, allFunctionsAndProgramsInSystem);
        this.allFunctionsAndProgramsInSystem = allFunctionsAndProgramsInSystem;
        this.mainProgramName = mainProgramName;
        this.mainProgramEngine = mainProgramEngine;
        checkForNameConflicts(allFunctionsAndProgramsInSystem, mainProgramName);

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
     * @throws FunctionNotFound     if an unknown function - not known in server or current program is called
     * @throws FunctionAlreadyExist if there is a name conflict with functions or programs in the server
     */
    public static FunctionManager createForProgram(@NotNull SProgram sProgram,
                                                   @NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                                                   @NotNull Engine mainProgramEngine,
                                                   String mainProgramName)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {

        return new FunctionManager(sProgram, allFunctionsAndProgramsInSystem, mainProgramEngine,
                mainProgramName);

    }

    public void finishInitialization() throws FunctionNotFound {
        finishInitQuotes();
        initialised = true;
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
     * @throws FunctionNotFound if a function is not found during the building process
     */

    private @NotNull Map<String, Engine> buildFunctions(@NotNull SProgram program,
                                                        @NotNull String mainProgramName,
                                                        @NotNull Engine mainProgramEngine,
                                                        @NotNull Map<String, Engine>
                                                                allFunctionsAndProgramsInSystem)
            throws LabelNotExist, FunctionNotFound {
        Map<String, Engine> functionMap = new HashMap<>();
        List<SFunction> sFunctions = program.getSFunctions().getSFunction();

        for (SFunction sFunc : sFunctions) {
            Engine engine = Engine.createFunctionEngine(sFunc, mainProgramName, this);
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

    public void checkForNameConflicts(@NotNull Map<String, Engine> allFunctionAndProgramsInSystem,
                                      @NotNull String functionName)
            throws FunctionAlreadyExist {

        for (String functionNameInSystem : allFunctionAndProgramsInSystem.keySet()) {
            // Prevent having functions with the same name as the main program and vice versa
            if (functionsInCurrentProgram.containsKey(functionNameInSystem) ||
                    functionNameInSystem.equals(mainProgramName)) {
                throw new FunctionAlreadyExist(mainProgramName, functionName);
            }
        }
    }

    public void addProgramAndFunctionsToSystem(@NotNull Map<String, Engine> allFunctionsAndProgramsInSystem,
                                               @NotNull Map<String, Engine> functionsInSystem) {
        allFunctionsAndProgramsInSystem.put(mainProgramName, mainProgramEngine);
        allFunctionsAndProgramsInSystem.putAll(functionsInCurrentProgram);
        functionsInSystem.putAll(functionsInCurrentProgram);
    }

    public @NotNull String getMainProgramName() {
        return mainProgramName;
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
}
