package dto.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public record VariableDTO(
        StringProperty name,
        IntegerProperty value,
        BooleanProperty hasChanged
) {
    // Backward compatibility constructor (existing code)
    public VariableDTO(StringProperty name, IntegerProperty value) {
        this(name, value, new SimpleBooleanProperty(false));
    }

    // New constructor with change tracking
    public VariableDTO(StringProperty name, IntegerProperty value, boolean hasChanged) {
        this(name, value, new SimpleBooleanProperty(hasChanged));
    }
}