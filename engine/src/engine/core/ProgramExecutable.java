package engine.core;

import java.util.List;
import java.util.Map;

/**
 * A {@code Package-private} record that encapsulates a program executable,
 * including its instructions and context map.
 */
record ProgramExecutable(
        List<Instruction> instructions,
        Map<String, Integer> contextMap
) {
}
