package ui.execution.summaryLine;

import dto.engine.InstructionDTO;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryLineController {

    // Observable properties for the counts
    private final IntegerProperty syntheticCount = new SimpleIntegerProperty(0);
    private final IntegerProperty basicCount = new SimpleIntegerProperty(0);

    @FXML
    public Label archICount;

    @FXML
    public Label archIICount;

    @FXML
    public Label archIIICount;

    @FXML
    public Label archIVCount;

    @FXML
    private Label syntheticCountLabel;

    @FXML
    private Label basicCountLabel;

    @FXML
    public void initialize() {
        // Bind labels to the properties for automatic updates
        syntheticCountLabel.textProperty().bind(syntheticCount.asString());
        basicCountLabel.textProperty().bind(basicCount.asString());

        // Initialize with zero counts
        updateCounts(null);
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
        Map<ArchitectureType, Integer> counts = new HashMap<>();

        // Count instructions by type
        for (InstructionDTO instruction : instructions) {
            if (instruction.type() == CommandType.SYNTHETIC) {
                syntheticInstructions++;
            } else if (instruction.type() == CommandType.BASIC) {
                basicInstructions++;
            }
            counts.put(instruction.architectureType(),
                    counts.getOrDefault(instruction.architectureType(), 0) + 1);
        }

        // Update the properties (this will automatically update the UI)
        syntheticCount.set(syntheticInstructions);
        basicCount.set(basicInstructions);
        archICount.setText(String.valueOf(counts.getOrDefault(ArchitectureType.ARCHITECTURE_I, 0)));
        archIICount.setText(String.valueOf(counts.getOrDefault(ArchitectureType.ARCHITECTURE_II, 0)));
        archIIICount.setText(String.valueOf(counts.getOrDefault(ArchitectureType.ARCHITECTURE_III, 0)));
        archIVCount.setText(String.valueOf(counts.getOrDefault(ArchitectureType.ARCHITECTURE_IV, 0)));


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
