package ui.web.jfx.task.program.load;

import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import javafx.application.Platform;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public record UIAdapter(
        Consumer<Boolean> programLoadedDelegate, Consumer<Boolean> argumentsEntered,
        Consumer<List<String>> variablesAndLabelsNamesDelegate,
        Consumer<List<InstructionDTO>> programInstructionsDelegate, Runnable clearDerivedInstructionsDelegate,
        Consumer<List<InstructionDTO>> summaryLineDelegate, Consumer<Integer> maxExpandLevelDelegate,
        Consumer<Integer> currentExpandLevelDelegate, Consumer<Integer> cyclesDelegate, Consumer<ProgramDTO> onFinish) {

    @Contract(value = " -> new", pure = true)
    public static @NotNull UIAdapterLoadFileTaskBuilder builder() {
        return new UIAdapterLoadFileTaskBuilder();
    }

    public void expandLevelsAndCyclesHandler(int maxExpandLevel) {
        Platform.runLater(() -> {
            maxExpandLevelDelegate.accept(maxExpandLevel);
            currentExpandLevelDelegate.accept(0);
            cyclesDelegate.accept(0);
        });
    }

    public void variablesAndInstructionsHandler(@NotNull ProgramDTO program) {
        Platform.runLater(() -> {
            programLoadedDelegate.accept(true);
            argumentsEntered.accept(false);
            variablesAndLabelsNamesDelegate.accept(program.allVariablesIncludingLabelsNames());
            programInstructionsDelegate.accept(program.instructions());
            clearDerivedInstructionsDelegate.run();
            summaryLineDelegate.accept(program.instructions());
        });
    }

    public static class UIAdapterLoadFileTaskBuilder {
        private Consumer<Boolean> programLoadedDelegate;
        private Consumer<Boolean> variablesEnteredDelegate;
        private Consumer<List<String>> variablesAndLabelsNamesDelegate;
        private Consumer<List<InstructionDTO>> programInstructionsDelegate;
        private Runnable clearDerivedInstructionsDelegate;
        private Consumer<List<InstructionDTO>> summaryLineDelegate;
        private Consumer<Integer> maxExpandLevelDelegate;
        private Consumer<Integer> currentExpandLevelDelegate;
        private Consumer<Integer> cyclesDelegate;
        private Consumer<ProgramDTO> onFinish;

        public UIAdapterLoadFileTaskBuilder programLoadedDelegate(Consumer<Boolean> programLoadedDelegate) {
            this.programLoadedDelegate = programLoadedDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder argumentsEnteredDelegate(Consumer<Boolean> variablesEnteredDelegate) {
            this.variablesEnteredDelegate = variablesEnteredDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder variablesAndLabelsNamesDelegate(Consumer<List<String>> variablesAndLabelsNamesDelegate) {
            this.variablesAndLabelsNamesDelegate = variablesAndLabelsNamesDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder programInstructionsDelegate(Consumer<List<InstructionDTO>> programInstructionsDelegate) {
            this.programInstructionsDelegate = programInstructionsDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder clearDerivedInstructionsDelegate(Runnable clearDerivedInstructionsDelegate) {
            this.clearDerivedInstructionsDelegate = clearDerivedInstructionsDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder summaryLineDelegate(Consumer<List<InstructionDTO>> summaryLineDelegate) {
            this.summaryLineDelegate = summaryLineDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder maxExpandLevelDelegate(Consumer<Integer> maxExpandLevelDelegate) {
            this.maxExpandLevelDelegate = maxExpandLevelDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder currentExpandLevelDelegate(Consumer<Integer> currentExpandLevelDelegate) {
            this.currentExpandLevelDelegate = currentExpandLevelDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder cyclesDelegate(Consumer<Integer> cyclesDelegate) {
            this.cyclesDelegate = cyclesDelegate;
            return this;
        }

        public UIAdapterLoadFileTaskBuilder onFinish(Consumer<ProgramDTO> onFinish) {
            this.onFinish = onFinish;
            return this;
        }

        public UIAdapter build() {
            return new UIAdapter(programLoadedDelegate, variablesEnteredDelegate, variablesAndLabelsNamesDelegate,
                    programInstructionsDelegate, clearDerivedInstructionsDelegate, summaryLineDelegate,
                    maxExpandLevelDelegate,
                    currentExpandLevelDelegate, cyclesDelegate, onFinish);
        }
    }
}
