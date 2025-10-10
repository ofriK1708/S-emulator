package logic.file.xml;

import engine.generated_2.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class XMLHandler {
    private final Unmarshaller unmarshaller;

    public XMLHandler() throws JAXBException {
        final String jaxbGeneratedPackageLoc = "engine.generated_2";
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbGeneratedPackageLoc);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SProgram unmarshallFile(@NotNull InputStream xmlFile) throws JAXBException {
        return (SProgram) unmarshaller.unmarshal(xmlFile);
    }
}
