package engine.core;

import engine.utils.ArchitectureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ExecutionMetadata(boolean isMainProgram,
                                int expandLevel,
                                @NotNull String programName,
                                @NotNull ArchitectureType architectureType) {

    public ExecutionMetadata(boolean isMainProgram,
                             int expandLevel,
                             @Nullable String programName,
                             @Nullable ArchitectureType architectureType) {
        this.isMainProgram = isMainProgram;
        this.expandLevel = expandLevel;
        this.programName = programName != null ? programName : "unknown";
        this.architectureType = architectureType != null ? architectureType : ArchitectureType.ARCHITECTURE_I; //
        // most basic
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean isMainProgram;
        private int expandLevel;
        private String programName;
        private ArchitectureType architectureType;

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

        public Builder expandLevel(int expandLevel) {
            this.expandLevel = expandLevel;
            return this;
        }

        public ExecutionMetadata build() {
            return new ExecutionMetadata(isMainProgram, expandLevel, programName, architectureType);
        }


        public boolean isMainProgram() {
            return isMainProgram;
        }

        public String getProgramName() {
            return programName;
        }

        public ArchitectureType getArchitectureType() {
            return architectureType;
        }
    }
}
