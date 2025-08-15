package backend.engine.example;

import backend.engine.Command;
import backend.engine.SEmulatorState;
import backend.engine.loader.InstructionData;
import backend.engine.loader.ProgramData;
import backend.engine.loader.XMLCommandLoader;
import backend.engine.loader.XMLLoaderException;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLLoaderExample {

    public static void main(String[] args) {
        try {
            // Create loader
            backend.engine.loader.XMLCommandLoader loader = new backend.engine.loader.XMLCommandLoader();

            // Load program from XML file
            ProgramData programData = loader.loadProgramFromXML("badic.xml");

            System.out.println("Loaded program: " + programData.getProgramName());
            System.out.println("Number of instructions: " + programData.getInstructions().size());
            System.out.println("Labels found: " + programData.getLabelMap());

            // Create command instances
            List<Command> commands = loader.createCommands(programData);

            // Example: Execute commands (simplified)
            Map<String, Integer> variables = new HashMap<>();
            executeProgram(commands, programData, variables);

            // Save state
            SEmulatorState state = new SEmulatorState(commands, programData, variables, 0);
            saveState(state, "emulator_state.ser");

            // Load state
            SEmulatorState loadedState = loadState("emulator_state.ser");
            System.out.println("State restored successfully");

        } catch (XMLLoaderException e) {
            System.err.println("Error loading XML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeProgram(List<Command> commands, ProgramData programData,
                                       Map<String, Integer> variables) {
        int pc = 0;
        List<InstructionData> instructions = programData.getInstructions();

        while (pc < commands.size()) {
            Command command = commands.get(pc);
            InstructionData instructionData = instructions.get(pc);

            System.out.println("Executing: " + instructionData.getCommandName() +
                    " on variable " + instructionData.getVariable());

            // Simple execution example - you'd need to implement full logic
            try {
                int result = command.execute(0); // Simplified - pass actual arguments
                System.out.println("Result: " + result);
                pc++;
            } catch (Exception e) {
                System.err.println("Execution error: " + e.getMessage());
                break;
            }
        }
    }

    private static void saveState(SEmulatorState state, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(state);
            System.out.println("State saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }

    private static SEmulatorState loadState(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (SEmulatorState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading state: " + e.getMessage());
            return null;
        }
    }
}