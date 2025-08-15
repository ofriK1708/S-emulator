package backend.engine.loader;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.basicCommands.*;
import backend.engine.syntheticCommands.*;
import java.io.Serializable;

/**
 * Factory class for creating Command instances based on their names and types
 */
public class CommandFactory implements Serializable {
    private static final long serialVersionUID = 1L;

    public Command createCommand(String commandName, CommandType commandType) {
        switch (commandName.toUpperCase()) {
            // Basic Commands
            case "NEUTRAL":
                return new Neutral();
            case "INCREASE":
                return new Increase();
            case "DECREASE":
                return new Decrease();
            case "JUMP_NOT_ZERO":
                return new JumpNotZero();

            // Synthetic Commands
            case "ZERO_VARIABLE":
                return new ZeroVariable();
            case "ASSIGNMENT":
                return new Assignment();
            case "GOTO_LABEL":
                return new GOTOLabel();
            case "CONSTANT_ASSIGNMENT":
                return new ConstantAssignment();
            case "JUMP_ZERO":
                return new JumpZero();
            case "JUMP_EQUAL_CONSTANT":
                return new JumpEqualConstant();

            default:
                throw new IllegalArgumentException("Unknown command: " + commandName);
        }
    }
}