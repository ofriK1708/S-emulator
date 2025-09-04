package ui.javafx.main;

import system.controller.controller.SystemController;

/**
 * Interface for component communication
 * Implements Observer pattern for loose coupling
 */
public interface ComponentEventListener {

    /**
     * Called when a file is successfully loaded
     */
    void onFileLoaded(SystemController systemController);

    /**
     * Called when the loaded file is cleaned/reset
     */
    void onFileCleaned();

    /**
     * Called when cycles count changes
     */
    void onCyclesChanged(int newCycleCount);
}