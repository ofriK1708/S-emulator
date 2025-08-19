package backend.system;

import backend.system.generated.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class XMLHandler {
    private final String jaxbGeneratedPackageLoc = "backend.system.generated";
    private final JAXBContext;
    private Unmarshaller unmarshaller;

    public XMLHandler() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(jaxbGeneratedPackageLoc);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SProgram loadProgram(Path xmlPath) throws JAXBException, FileNotFoundException {
        InputStream xmlFile = new FileInputStream(xmlPath.toString());
        return (SProgram) unmarshaller.unmarshal(xmlFile);
    }

    public boolean validateXMLStructure(String xmlPath) {
        try {
            loadProgram(xmlPath);
            return true;
        } catch (JAXBException e) {
            return false;
        }
    }
}
