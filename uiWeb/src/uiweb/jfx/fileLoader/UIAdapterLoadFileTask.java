package uiweb.jfx.fileLoader;

import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public record UIAdapterLoadFileTask(
        Consumer<Boolean> programLoadedDelegate, Consumer<Boolean> variablesEnteredDelegate,
        Consumer<List<String>> variablesAndLabelsNamesDelegate,
        Consumer<List<InstructionDTO>> programInstructionsDelegate, Runnable clearDerivedInstructionsDelegate,
        Consumer<List<InstructionDTO>> summaryLineDelegate, Consumer<Integer> maxExpandLevelDelegate,
        Consumer<Integer> currentExpandLevelDelegate, Consumer<Integer> cyclesDelegate, Consumer<ProgramDTO> onFinish) {

    public void variablesAndInstructionsHandler(@NotNull ProgramDTO program, List<String> allVars) {
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
}
