package ui.jfx.fileLoader;

import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;

import java.util.List;
import java.util.function.Consumer;

public class UIAdapterLoadFileTask {
    public Consumer<Boolean> programLoadedDelegate;
    public Consumer<Boolean> variablesEnteredDelegate;
    public Consumer<List<String>> variablesAndLabelsNamesDelegate;
    public Consumer<List<InstructionDTO>> programInstructionsDelegate;
    public Runnable clearDerivedInstructionsDelegate;
    public Consumer<List<InstructionDTO>> summaryLineDelegate;
    public Consumer<Integer> maxExpandLevelDelegate;
    public Consumer<Integer> currentExpandLevelDelegate;
    public Consumer<Integer> cyclesDelegate;
    public Consumer<ProgramDTO> onFinish;

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
}
