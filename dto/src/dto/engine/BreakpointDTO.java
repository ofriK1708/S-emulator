package dto.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data Transfer Object representing a breakpoint in the debugger.
 * Immutable record following the existing DTO pattern in the project.
 */
public record BreakpointDTO(
        int lineNumber,           // 0-based instruction index
        boolean enabled,          // Whether breakpoint is active
        @Nullable String condition, // Optional condition (null for unconditional)
        @NotNull BreakpointStatus status // Current status of the breakpoint
) {
    /**
     * Creates an unconditional, enabled breakpoint.
     */
    public static BreakpointDTO createSimple(int lineNumber) {
        return new BreakpointDTO(lineNumber, true, null, BreakpointStatus.ACTIVE);
    }
    /**
     * Creates a copy with updated status.
     */
    public BreakpointDTO withStatus(@NotNull BreakpointStatus status) {
        return new BreakpointDTO(lineNumber, enabled, condition, status);
    }

    /**
     * Status of a breakpoint during execution.
     */
    public enum BreakpointStatus {
        ACTIVE,      // Breakpoint is waiting to be hit
        HIT,         // Breakpoint was just hit (execution paused here)
        DISABLED,    // Breakpoint exists but is disabled
        INVALID      // Breakpoint line is invalid (e.g., out of bounds)
    }
}