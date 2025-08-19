package backend.system;

import backend.engine.ProgramEngine;
import backend.system.generated.SProgram;
import jakarta.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemController {
    private XMLHandler xmlHandler;
    private List<ProgramEngine> activeEngines;

    public SystemController() throws JAXBException {
        this.xmlHandler = new XMLHandler();
        this.activeEngines = new CopyOnWriteArrayList<>();
    }



    public List<ProgramEngine> getActiveEngines() {
        // Clean up inactive engines
        Iterator<ProgramEngine> iterator = activeEngines.iterator();
        while (iterator.hasNext()) {
            ProgramEngine engine = iterator.next();
          //  if (!ProgramEngine.isActive()) {
             //   iterator.remove();
            //}
        }

        return new ArrayList<>(activeEngines);
    }

    public void cleanup() {
        for (ProgramEngine engine : activeEngines) {
           // engine.shutdown();
        }
        activeEngines.clear();
    }

    public void shutdownEngine(ProgramEngine engine) {
        if (activeEngines.contains(engine)) {
           // engine.shutdown();
            activeEngines.remove(engine);
        }
    }

    public void resetAllEngines() {
        for (ProgramEngine engine : activeEngines) {
            //engine.reset();
        }
    }

    public int getActiveEngineCount() {
        return getActiveEngines().size();
    }

    public ProgramEngine createEngine(SProgram program) {

        ProgramEngine engine = new ProgramEngine(program);
        activeEngines.add(engine);
        return engine;
    }
}