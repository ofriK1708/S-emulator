package backend.engine;

import java.util.List;
import java.util.Map;

public interface Command
{
    void execute(Map<String, String> args);
    int getCycles();
    CommandType getType();
    List<Command> expand(int level);
    int getNumberOfArgs();
    String toString(); // TODO: ask TO KEEP ?
    String getDisplayFormat(Map<String,String> args);
}
