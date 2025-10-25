package dto.server;

import dto.engine.DebugStateChangeResultDTO;
import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.FullExecutionResultDTO;
import dto.engine.ProgramDTO;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * A response object representing the result of a system operation.
 * This class contains the following fields:
 *
 * @param isSuccess                 Indicates whether the operation was successful.
 * @param message                   A message providing additional information about the operation result.
 * @param programDTO                An optional ProgramDTO object representing the programDTO (Instruction, name, etc.).
 *                                  can be null if not needed.
 * @param debugStateChangeResultDTO An optional DebugStateChangeResultDTO object representing the debug state change
 *                                  result.
 *                                  can be null if not needed.
 * @param allUsersDTO               An optional set of UserDTO objects representing all users in the system.
 *                                  can be null if not needed.
 * @param userStatisticsDTOList     An optional list of ExecutionResultStatisticsDTO objects representing user
 *                                  execution statistics.
 *                                  can be null if not needed.
 * @param fullExecutionResultDTO    A FullExecutionResultDTO object representing the full execution result.
 */
public record SystemResponse(boolean isSuccess, @NotNull String message, @Nullable ProgramDTO programDTO,
                             @Nullable DebugStateChangeResultDTO debugStateChangeResultDTO,
                             @Nullable Set<UserDTO> allUsersDTO,
                             @Nullable List<ExecutionResultStatisticsDTO> userStatisticsDTOList,
                             @Nullable FullExecutionResultDTO fullExecutionResultDTO) {
    /**
     * Indicates whether the operation was successful.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean isSuccessful() {
        return isSuccess;
    }

    /**
     * Returns the message providing additional information about the operation result.
     *
     * @return the message string
     */
    @Override
    public @NotNull String message() {
        return message;
    }

    /**
     * Returns the ProgramDTO object associated with the response.
     *
     * @return the ProgramDTO object
     * @throws IllegalStateException if the ProgramDTO is not available in this response
     */
    @Override
    public @NotNull ProgramDTO programDTO() {
        if (programDTO == null) {
            throw new IllegalStateException("ProgramDTO is not available in this response.");
        }
        return programDTO;
    }

    /**
     * Returns the DebugStateChangeResultDTO object associated with the response.
     *
     * @return the DebugStateChangeResultDTO object
     * @throws IllegalStateException if the DebugStateChangeResultDTO is not available in this response
     */
    @Override
    public @NotNull DebugStateChangeResultDTO debugStateChangeResultDTO() {
        if (debugStateChangeResultDTO == null) {
            throw new IllegalStateException("DebugStateChangeResultDTO is not available in this response.");
        }
        return debugStateChangeResultDTO;
    }

    /**
     * Returns the set of UserDTO objects representing all users in the system.
     *
     * @return the set of UserDTO objects
     * @throws IllegalStateException if the AllUsersDTO is not available in this response
     */
    @Override
    public @NotNull Set<UserDTO> allUsersDTO() {
        if (allUsersDTO == null) {
            throw new IllegalStateException("AllUsersDTO is not available in this response.");
        }
        return allUsersDTO;
    }

    /**
     * Returns the list of ExecutionResultStatisticsDTO objects representing user execution statistics.
     *
     * @return the list of ExecutionResultStatisticsDTO objects
     * @throws IllegalStateException if the UserStatisticsDTOList is not available in this response
     */
    @Override
    public @NotNull List<ExecutionResultStatisticsDTO> userStatisticsDTOList() {
        if (userStatisticsDTOList == null) {
            throw new IllegalStateException("UserStatisticsDTOList is not available in this response.");
        }
        return userStatisticsDTOList;
    }

    @Override
    public @NotNull FullExecutionResultDTO fullExecutionResultDTO() {
        if (fullExecutionResultDTO == null) {
            throw new IllegalStateException("FullExecutionResultDTO is not available in this response.");
        }
        return fullExecutionResultDTO;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * A builder class for constructing SystemResponse objects.
     *
     * @see SystemResponse
     */
    public static class Builder {
        private @Nullable Boolean isSuccess;
        private @NotNull String message = "";
        private @Nullable ProgramDTO programDTO;
        private @Nullable DebugStateChangeResultDTO debugStateChangeResultDTO;
        private @Nullable Set<UserDTO> allUsersDTO;
        private @Nullable List<ExecutionResultStatisticsDTO> userStatisticsDTOList;
        private @Nullable FullExecutionResultDTO fullExecutionResultDTO;

        /**
         * Sets the {@link SystemResponse#isSuccess} for the SystemResponse.
         *
         * @param success the success status to set
         * @return the Builder instance
         * @see SystemResponse
         */
        public Builder isSuccess(boolean success) {
            this.isSuccess = success;
            return this;
        }

        /**
         * Sets the {@link SystemResponse#message} for the SystemResponse.
         *
         * @param message the message to set
         * @return the Builder instance
         * @see SystemResponse
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the {@link SystemResponse#programDTO} for the SystemResponse
         *
         * @param program the ProgramDTO to set
         * @return the Builder instance
         * @see ProgramDTO
         * @see SystemResponse
         */
        public Builder programDTO(@NotNull ProgramDTO program) {
            this.programDTO = program;
            return this;
        }

        /**
         * Sets the {@link SystemResponse#debugStateChangeResultDTO} for the SystemResponse
         *
         * @param debugStateChangeResultDTO the DebugStateChangeResultDTO to set
         * @return the Builder instance
         * @see DebugStateChangeResultDTO
         * @see SystemResponse
         */
        public Builder debugStateChangeResultDTO(@NotNull DebugStateChangeResultDTO debugStateChangeResultDTO) {
            this.debugStateChangeResultDTO = debugStateChangeResultDTO;
            return this;
        }

        /**
         * Sets the {@link SystemResponse#allUsersDTO} for the SystemResponse
         *
         * @param allUsersDTO the set of UserDTO to set
         * @return the Builder instance
         * @see UserDTO
         * @see SystemResponse
         */
        public Builder allUsersDTO(@NotNull Set<UserDTO> allUsersDTO) {
            this.allUsersDTO = allUsersDTO;
            return this;
        }

        /**
         * Sets the {@link SystemResponse#userStatisticsDTOList} for the SystemResponse
         *
         * @param userStatisticsDTOList the list of ExecutionResultStatisticsDTO to set
         * @return the Builder instance
         * @see ExecutionResultStatisticsDTO
         * @see SystemResponse
         */
        public Builder userStatisticsDTOList(@NotNull List<ExecutionResultStatisticsDTO> userStatisticsDTOList) {
            this.userStatisticsDTOList = userStatisticsDTOList;
            return this;
        }

        /**
         * Sets the {@link SystemResponse#fullExecutionResultDTO} for the SystemResponse
         *
         * @param fullExecutionResultDTO the FullExecutionResultDTO to set
         * @return the Builder instance
         * @see FullExecutionResultDTO
         * @see SystemResponse
         */
        public Builder fullExecutionResultDTO(@NotNull FullExecutionResultDTO fullExecutionResultDTO) {
            this.fullExecutionResultDTO = fullExecutionResultDTO;
            return this;
        }

        /**
         * Builds and returns the SystemResponse object.
         *
         * @return the constructed SystemResponse
         */
        public SystemResponse build() {
            if (isSuccess == null) {
                throw new IllegalStateException("isSuccess must be set");
            }
            return new SystemResponse(isSuccess, message, programDTO, debugStateChangeResultDTO,
                    allUsersDTO, userStatisticsDTOList, fullExecutionResultDTO);
        }
    }
}
