package engine.exception;

import engine.utils.ArchitectureType;

public class IllegalArchitectureType extends RuntimeException {
    public IllegalArchitectureType(String message, ArchitectureType requiredArchitecture,
                                   ArchitectureType providedArchitecture) {
        super(String.format("%s: required architecture type is %s, but provided is %s",
                message, requiredArchitecture, providedArchitecture));
    }
}
