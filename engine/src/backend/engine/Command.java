package backend.engine;

import java.util.List;

public interface Command
{
    int execute(Object... args);
    int getCycles();
    CommandType getType();
    List<Command> expand(int level);
    int getNumberOfArgs();
    String toString(); // TODO: ask TO KEEP ?
    String getDisplayFormat(Object... argsNames);
}
