package backend.engine.loader;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Data class representing an instruction loaded from XML
 */
public class InstructionData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String commandName;
    private final String commandType;
    private final String variable;
    private final Optional<String> label;
    private final List<ArgumentData> arguments;

    public InstructionData(String commandName, String commandType, String variable,
                           Optional<String> label, List<ArgumentData> arguments) {
        this.commandName = commandName;
        this.commandType = commandType;
        this.variable = variable;
        this.label = label;
        this.arguments = arguments;
    }

    public String getCommandName() { return commandName; }
    public String getCommandType() { return commandType; }
    public String getVariable() { return variable; }
    public Optional<String> getLabel() { return label; }
    public List<ArgumentData> getArguments() { return arguments; }

    public static class ArgumentData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final String value;

        public ArgumentData(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public String getValue() { return value; }
    }
}