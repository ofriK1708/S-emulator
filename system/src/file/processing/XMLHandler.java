package file.processing;

import generated.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLHandler
{
    private final Unmarshaller unmarshaller;

    public XMLHandler() throws JAXBException
    {

        final String jaxbGeneratedPackageLoc = "generated";
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbGeneratedPackageLoc);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SProgram unmarshallFile(Path xmlPath) throws JAXBException, IOException
    {
        InputStream xmlFile = Files.newInputStream(xmlPath);
        return (SProgram) unmarshaller.unmarshal(xmlFile);
    }
}
