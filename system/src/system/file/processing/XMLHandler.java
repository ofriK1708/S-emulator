package system.file.processing;

import engine.generated_2.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLHandler
{
    private final Unmarshaller unmarshaller;

    public XMLHandler() throws JAXBException
    {

        final String jaxbGeneratedPackageLoc = "engine.generated_2";
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbGeneratedPackageLoc);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SProgram unmarshallFile(@NotNull Path xmlPath) throws JAXBException, IOException
    {
        InputStream xmlFile = Files.newInputStream(xmlPath);
        return (SProgram) unmarshaller.unmarshal(xmlFile);
    }
}
