package system.controller;

import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface EngineProgramManager {
    void LoadProgramFromFile(@NotNull Path xmlFilePath) throws LabelNotExist, JAXBException, IOException,
            FunctionNotFound;
}
