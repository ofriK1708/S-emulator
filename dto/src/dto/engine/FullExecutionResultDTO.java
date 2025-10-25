package dto.engine;

import engine.utils.ArchitectureType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import static engine.utils.ProgramUtils.OUTPUT_NAME;

/**
 * A Data Transfer Object (DTO) that encapsulates the full execution result of a program.
 *
 * @param isMainProgram    Indicates if the executed program is the main program.
 * @param programName      The name of the executed program.
 * @param architectureType The architecture type used during execution.
 * @param arguments        A map of argument (x1,x2,x3,...) names to their integer sorted by their numeric suffixes
 * @param workVariables    A map of work variable (z1,z2,...) names to their integer sorted by their numeric suffixes
 * @param output           The output (y) value produced by the program.
 * @param expandLevel      The level of expansion used during execution.
 * @param cycleCount       The number of cycles taken during execution.
 * @param creditsCost      The cost in credits for executing the program.
 */
public record FullExecutionResultDTO(boolean isMainProgram,
                                     String programName,
                                     ArchitectureType architectureType,
                                     Map<String, Integer> arguments,
                                     Map<String, Integer> workVariables,
                                     int output,
                                     int expandLevel,
                                     int cycleCount,
                                     int creditsCost) {

    @Contract("_, _, _, _, _ -> new")
    private static @NotNull FullExecutionResultDTO from(@NotNull ExecutionResultValuesDTO valuesDTO, int expandLevel,
                                                        boolean isMainProgram,
                                                        String programName, ArchitectureType architectureType) {
        return new FullExecutionResultDTO(
                isMainProgram,
                programName,
                architectureType,
                valuesDTO.arguments(),
                valuesDTO.workVariables(),
                valuesDTO.output(),
                expandLevel,
                valuesDTO.cycleCount(),
                valuesDTO.creditsCost()
        );
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Combines output, arguments, and work variables into a single map sorted by variable names.
     * the order is output first, then arguments, then work variables. <strong>(y, x1,x2,..., z1,z2,...)</strong>
     *
     * @return A map containing all variables with their names as keys and their integer values.
     */
    public Map<String, Integer> getAllVariablesSorted() {
        Map<String, Integer> allVars = new LinkedHashMap<>();
        allVars.put(OUTPUT_NAME, output);
        allVars.putAll(arguments);
        allVars.putAll(workVariables);
        return allVars;
    }

    public static class Builder {
        private ExecutionResultValuesDTO valuesDTO;
        private int expandLevel;
        private boolean isMainProgram;
        private String programName;
        private ArchitectureType architectureType;

        public Builder valuesDTO(ExecutionResultValuesDTO valuesDTO) {
            this.valuesDTO = valuesDTO;
            return this;
        }

        public Builder expandLevel(int expandLevel) {
            this.expandLevel = expandLevel;
            return this;
        }

        public Builder isMainProgram(boolean isMainProgram) {
            this.isMainProgram = isMainProgram;
            return this;
        }

        public Builder programName(String programName) {
            this.programName = programName;
            return this;
        }

        public Builder architectureType(ArchitectureType architectureType) {
            this.architectureType = architectureType;
            return this;
        }

        public FullExecutionResultDTO build() {
            return FullExecutionResultDTO.from(valuesDTO, expandLevel, isMainProgram, programName, architectureType);
        }
    }
}
