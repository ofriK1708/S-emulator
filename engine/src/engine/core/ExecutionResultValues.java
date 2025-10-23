package engine.core;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ExecutionResultValues {
    int output = 0;
    @NotNull Map<String, Integer> arguments;
    @NotNull Map<String, Integer> workVariables = new HashMap<>();

    public ExecutionResultValues(@NotNull Map<String, Integer> arguments) {
        this.arguments = arguments;
    }

    public ExecutionResultValues(int output,
                                 @NotNull Map<String, Integer> arguments,
                                 @NotNull Map<String, Integer> workVariables) {
        this.output = output;
        this.arguments = arguments;
        this.workVariables = workVariables;
    }

    public int getOutput() {
        return output;
    }

    void setOutput(int output) {
        this.output = output;
    }

    public @NotNull Map<String, Integer> getArguments() {
        return arguments;
    }

    void setArguments(@NotNull Map<String, Integer> arguments) {
        this.arguments = arguments;
    }

    public @NotNull Map<String, Integer> getWorkVariables() {
        return workVariables;
    }

    void setWorkVariables(@NotNull Map<String, Integer> workVariables) {
        this.workVariables = workVariables;
    }
}
