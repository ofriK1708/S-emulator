package engine.core;

import engine.core.syntheticCommand.Quote;
import engine.exception.FunctionAlreadyExist;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SFunction;
import engine.generated_2.SProgram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FunctionManager {
    private @NotNull
    final ProgramEngine mainProgramEngine;
    private final @NotNull String mainProgramName;
    private final @NotNull Map<String, ProgramEngine> functionsInCurrentProgram;
    private final List<Quote> uninitializedQuotes = new ArrayList<>();
    private @Nullable Map<String, ProgramEngine> allFunctionsAndProgramsInSystem = null;
    private boolean isFinishedInit = false;

    public FunctionManager(@NotNull String mainProgramName, @NotNull ProgramEngine mainProgramEngine) {
        this.mainProgramEngine = mainProgramEngine;
        this.functionsInCurrentProgram = new HashMap<>();
        this.mainProgramName = mainProgramName;
    }

    private FunctionManager(@NotNull Map<String, ProgramEngine> functionsInCurrentProgram,
                            @NotNull Map<String, ProgramEngine> allFunctionsAndProgramsInSystem,
                            @NotNull ProgramEngine mainProgramEngine,
                            @NotNull String mainProgramName) throws FunctionNotFound, FunctionAlreadyExist {
        this.functionsInCurrentProgram = functionsInCurrentProgram;
        this.mainProgramName = mainProgramName;
        this.mainProgramEngine = mainProgramEngine;
        finishInitFunctionManager(allFunctionsAndProgramsInSystem);

    }

    public static FunctionManager createFrom(@NotNull SProgram program,
                                             @NotNull Map<String, ProgramEngine> allFunctionsAndProgramsInSystem,
                                             @NotNull ProgramEngine mainProgramEngine,
                                             String mainProgramName)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {

        Map<String, ProgramEngine> functionsInCurrentProgram = buildFunctions(program, mainProgramName,
                mainProgramEngine);

        functionsInCurrentProgram.values().forEach(func ->
                func.getFunctionManager().setFunctions(functionsInCurrentProgram, func.getProgramName()));

        return new FunctionManager(functionsInCurrentProgram, allFunctionsAndProgramsInSystem, mainProgramEngine,
                mainProgramName);

    }

    private static @NotNull Map<String, ProgramEngine> buildFunctions(@NotNull SProgram program,
                                                                      @NotNull String mainProgramName,
                                                                      @NotNull ProgramEngine mainProgramEngine)
            throws LabelNotExist, FunctionNotFound {
        Map<String, ProgramEngine> functionMap = new HashMap<>();
        List<SFunction> sFunctions = program.getSFunctions().getSFunction();

        for (SFunction sFunc : sFunctions) {
            ProgramEngine engine = new ProgramEngine(sFunc, mainProgramName, mainProgramEngine);
            functionMap.put(engine.getProgramName(), engine);
        }

        return functionMap;
    }

    public boolean isFinishedInit() {
        return isFinishedInit;
    }

    public void addToUninitializedQuotes(Quote quote) {
        uninitializedQuotes.add(quote);
    }

    void setFunctions(@NotNull Map<String, ProgramEngine> allFunctionsInMain,
                      @NotNull String programName) {
        this.allFunctionsAndProgramsInSystem = allFunctionsInMain.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(programName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void finishInitFunctionManager(@NotNull Map<String, ProgramEngine> allFunctionAndProgramsInSystem)
            throws FunctionNotFound, FunctionAlreadyExist {
        this.addSystemFunctionsAndFinishInitQuote(allFunctionAndProgramsInSystem, mainProgramName);
        for (Map.Entry<String, ProgramEngine> function : functionsInCurrentProgram.entrySet()) {
            function.getValue().getFunctionManager().addSystemFunctionsAndFinishInitQuote(allFunctionAndProgramsInSystem,
                    function.getValue().getProgramName());
        }
    }

    private void addSystemFunctionsAndFinishInitQuote(@NotNull Map<String, ProgramEngine> allFunctionAndProgramsInSystem,
                                                      @NotNull String functionName)
            throws FunctionAlreadyExist, FunctionNotFound {
        addSystemFunctionsAndPrograms(allFunctionAndProgramsInSystem, functionName);
        isFinishedInit = true;
        for (Quote quote : uninitializedQuotes) {
            quote.initAndValidateQuote();
        }
        uninitializedQuotes.clear();
    }

    public void addSystemFunctionsAndPrograms(@NotNull Map<String, ProgramEngine> allFunctionAndProgramsInSystem,
                                              @NotNull String functionName)
            throws FunctionAlreadyExist {

        if (this.allFunctionsAndProgramsInSystem == null) {
            this.allFunctionsAndProgramsInSystem = new HashMap<>(functionsInCurrentProgram);
        }

        for (Map.Entry<String, ProgramEngine> engineEntry : allFunctionAndProgramsInSystem.entrySet()) {
            // Prevent having functions with the same name as the main program and vice versa
            if (functionsInCurrentProgram.containsKey(engineEntry.getKey()) ||
                    engineEntry.getKey().equals(mainProgramName)) {
                throw new FunctionAlreadyExist(mainProgramName, functionName);
            }
            allFunctionsAndProgramsInSystem.put(engineEntry.getKey(), engineEntry.getValue());
        }
    }

    public Map<String, ProgramEngine> getAllFunctionsAndProgramsInSystem() {
        return Objects.requireNonNullElseGet(allFunctionsAndProgramsInSystem, HashMap::new);
    }

    public @NotNull String getMainProgramName() {
        return mainProgramName;
    }

    public @NotNull ProgramEngine getMainProgramEngine() {
        return mainProgramEngine;
    }
}
