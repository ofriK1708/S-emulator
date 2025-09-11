package dto.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public record VariableDTO(StringProperty name, IntegerProperty value) {
}
