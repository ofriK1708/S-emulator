package ui.jfx.fileLoader;

import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import javafx.application.Platform;

import java.util.List;
import java.util.function.Consumer;

public class UIAdapterLoadFileTask {
    private final Consumer<Boolean> programLoadedDelegate;
    private final Consumer<Boolean> variablesEnteredDelegate;
    private final Consumer<List<String>> variablesAndLabelsNamesDelegate;
    private final Consumer<List<InstructionDTO>> programInstructionsDelegate;
    private final Runnable clearDerivedInstructionsDelegate;
    private final Consumer<List<InstructionDTO>> summaryLineDelegate;
    private final Consumer<Integer> maxExpandLevelDelegate;
    private final Consumer<Integer> currentExpandLevelDelegate;
    private final Consumer<Integer> cyclesDelegate;
    private final Consumer<ProgramDTO> onFinish;

    public UIAdapterLoadFileTask(Consumer<Boolean> programLoadedDelegate, Consumer<Boolean> variablesEnteredDelegate, Consumer<List<String>> variablesAndLabelsNamesDelegate, Consumer<List<InstructionDTO>> programInstructionsDelegate, Runnable clearDerivedInstructionsDelegate, Consumer<List<InstructionDTO>> summaryLineDelegate, Consumer<Integer> maxExpandLevelDelegate, Consumer<Integer> currentExpandLevelDelegate, Consumer<Integer> cyclesDelegate, Consumer<ProgramDTO> onFinish) {
        this.programLoadedDelegate = programLoadedDelegate;
        this.variablesEnteredDelegate = variablesEnteredDelegate;
        this.variablesAndLabelsNamesDelegate = variablesAndLabelsNamesDelegate;
        this.programInstructionsDelegate = programInstructionsDelegate;
        this.clearDerivedInstructionsDelegate = clearDerivedInstructionsDelegate;
        this.summaryLineDelegate = summaryLineDelegate;
        this.maxExpandLevelDelegate = maxExpandLevelDelegate;
        this.currentExpandLevelDelegate = currentExpandLevelDelegate;
        this.cyclesDelegate = cyclesDelegate;
        this.onFinish = onFinish;
    }

    public void variablesAndInstructionsHandler(ProgramDTO program, List<String> allVars) {
        Platform.runLater(() -> {
            programLoadedDelegate.accept(true);
            variablesEnteredDelegate.accept(false);
            variablesAndLabelsNamesDelegate.accept(allVars);
            programInstructionsDelegate.accept(program.instructions());
            clearDerivedInstructionsDelegate.run();
            summaryLineDelegate.accept(program.instructions());
        });
    }

    public void expandLevelsAndCyclesHandler(int maxExpandLevel) {
        Platform.runLater(() -> {
            maxExpandLevelDelegate.accept(maxExpandLevel);
            currentExpandLevelDelegate.accept(0);
            cyclesDelegate.accept(0);
        });
    }

    public Consumer<ProgramDTO> getOnFinish() {
        return onFinish;
    }
}
