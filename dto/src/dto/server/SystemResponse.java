package dto.server;

import dto.engine.DebugStateChangeResultDTO;
import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.ProgramDTO;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * A response object representing the result of a system operation.
 * This class contains the following fields:
 * <ul>
 *     <li>{@code isSuccess}: Indicates whether the operation was successful. (This field is mandatory).</li>
 *     <li>{@code message}: A message providing details about the operation's result
 *     (e.g., isSuccess message or error details).</li>
 *     <li>{@code programDTO}: The programDTO data associated with the response, if applicable. Can be null.</li>
 *     <li>{@code debugStateChangeResultDTO}: The debug state change result data associated with the response,
 *     if applicable. Can be null.</li>
 * </ul>
 */
public class SystemResponse {
    /**
     * Indicates whether the operation was successful.
     *
     */
    private final boolean isSuccess;

    /**
     * A message providing additional information about the operation result.
     *
     */
    private final @NotNull String message;

    /**
     * An optional ProgramDTO object representing the programDTO (Instruction, name, etc.).
     * can be null if not needed.
     *
     */
    private final @Nullable ProgramDTO programDTO;

    /**
     * An optional DebugStateChangeResultDTO object representing the debug state change result.
     * can be null if not needed.
     *
     * @see DebugStateChangeResultDTO
     */
    private final @Nullable DebugStateChangeResultDTO debugStateChangeResultDTO;

    /**
     * An optional set of UserDTO objects representing all users in the system.
     * can be null if not needed.
     *
     * @see UserDTO
     */
    private final @Nullable Set<UserDTO> allUsersDTO;

    /**
     * An optional list of ExecutionResultStatisticsDTO objects representing user execution statistics.
     * can be null if not needed.
     *
     * @see ExecutionResultStatisticsDTO
     */
    private final @Nullable List<ExecutionResultStatisticsDTO> userStatisticsDTOList;

    private SystemResponse(Boolean isSuccess,
                           @Nullable String message,
                           @Nullable ProgramDTO programDTO,
                           @Nullable DebugStateChangeResultDTO debugStateChangeResultDTO,
                           @Nullable Set<UserDTO> allUsersDTO,
                           @Nullable List<ExecutionResultStatisticsDTO> userStatisticsDTOList) {
        if (isSuccess == null) {
            throw new IllegalArgumentException("Success field must be provided and cannot be null.");
        }
        this.isSuccess = isSuccess;
        this.message = message != null ? message : "";
        this.programDTO = programDTO;
        this.debugStateChangeResultDTO = debugStateChangeResultDTO;
        this.allUsersDTO = allUsersDTO;
        this.userStatisticsDTOList = userStatisticsDTOList;

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
        private @Nullable String message;
        private @Nullable ProgramDTO programDTO;
        private @Nullable DebugStateChangeResultDTO debugStateChangeResultDTO;
        private @Nullable Set<UserDTO> allUsersDTO;
        private @Nullable List<ExecutionResultStatisticsDTO> userStatisticsDTOList;

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
         * Builds and returns the SystemResponse object.
         *
         * @return the constructed SystemResponse
         */
        public SystemResponse build() {
            return new SystemResponse(isSuccess, message, programDTO, debugStateChangeResultDTO,
                    allUsersDTO, userStatisticsDTOList);
        }
    }
}
