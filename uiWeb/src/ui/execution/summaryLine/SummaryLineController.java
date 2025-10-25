package ui.execution.summaryLine;

import dto.engine.InstructionDTO;
import engine.utils.CommandType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SummaryLineController {

    // Observable properties for the counts
    private final IntegerProperty syntheticCount = new SimpleIntegerProperty(0);
    private final IntegerProperty basicCount = new SimpleIntegerProperty(0);
    @FXML
    private Label syntheticCountLabel;
    @FXML
    private Label basicCountLabel;
    @FXML
    private Label programLoadedLabel;

    @FXML
    public void initialize() {
        // Bind labels to the properties for automatic updates
        syntheticCountLabel.textProperty().bind(syntheticCount.asString());
        basicCountLabel.textProperty().bind(basicCount.asString());

        // Initialize with zero counts
        updateCounts(null);
    }

    public void initComponent(StringProperty loadedProgramName) {
        // Bind the program loaded label to show the current program name or "No Program Loaded"
        programLoadedLabel.textProperty().bind(
                Bindings.when(loadedProgramName.isEmpty())
                        .then("No Program Loaded")
                        .otherwise(loadedProgramName)
        );
    }

    /**
     * Updates the instruction counts based on the provided instruction list
     *
     * @param instructions List of InstructionDTO objects to analyze
     */
    public void updateCounts(@Nullable List<InstructionDTO> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            syntheticCount.set(0);
            basicCount.set(0);
            return;
        }

        int syntheticInstructions = 0;
        int basicInstructions = 0;

        // Count instructions by type
        for (InstructionDTO instruction : instructions) {
            if (instruction.type() == CommandType.SYNTHETIC) {
                syntheticInstructions++;
            } else if (instruction.type() == CommandType.BASIC) {
                basicInstructions++;
            }
        }

        // Update the properties (this will automatically update the UI)
        syntheticCount.set(syntheticInstructions);
        basicCount.set(basicInstructions);

        System.out.println("SummaryLine updated - Synthetic: " + syntheticInstructions +
                ", Basic: " + basicInstructions);
    }

    /**
     * Clears the counts (sets both to 0)
     */
    public void clearCounts() {
        syntheticCount.set(0);
        basicCount.set(0);
    }
}
