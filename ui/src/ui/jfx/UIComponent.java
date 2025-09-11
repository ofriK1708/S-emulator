package ui.jfx;

import java.util.function.Consumer;

public interface UIComponent<T> {
    void initComponent(Consumer<T> callback);
}
