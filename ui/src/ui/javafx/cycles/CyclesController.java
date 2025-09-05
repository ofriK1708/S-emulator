package ui.javafx.cycles;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import ui.javafx.main.ComponentEventListener;
import ui.javafx.main.MainController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Cycles Controller - manages cycle count display
 * Integrates with MainController architecture
 */
public class CyclesController implements Initializable {

    @FXML private Label numOfCycles;

    private int currentCycles = 0;

    // Component communication
    private ComponentEventListener eventListener;
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupInitialState();
    }

    private void setupInitialState() {
        updateCyclesDisplay(0);
    }

    /**
     * Update the cycles count
     */
    public void updateCycles(int cycleCount) {
        this.currentCycles = cycleCount;
        updateCyclesDisplay(cycleCount);

        // Notify main controller
        if (eventListener != null) {
            eventListener.onCyclesChanged(cycleCount);
        }
    }

    /**
     * Reset cycles to zero
     */
    public void resetCycles() {
        updateCycles(0);
    }

    /**
     * Update the UI display
     */
    private void updateCyclesDisplay(int cycles) {
        if (numOfCycles != null) {
            numOfCycles.setText(String.valueOf(cycles));
        }
    }

    /**
     * Get current cycles count
     */
    public int getCurrentCycles() {
        return currentCycles;
    }

    // ===== COMPONENT COMMUNICATION METHODS =====

    public void setEventListener(ComponentEventListener listener) {
        this.eventListener = listener;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}