package backend.system;

import backend.system.generated.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class XMLHandler {
    private final Unmarshaller unmarshaller;

    public XMLHandler() throws JAXBException {
        String jaxbGeneratedPackageLoc = "backend.system.generated";
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbGeneratedPackageLoc);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SProgram unmaeshalleForm(Path xmlPath) throws JAXBException, FileNotFoundException {
        InputStream xmlFile = new FileInputStream(xmlPath.toString());
        return (SProgram) unmarshaller.unmarshal(xmlFile);
    }

    public boolean validateXMLStructure(Path xmlPath) {
        try {
            unmaeshalleForm(xmlPath);
            return true;
        } catch (JAXBException | FileNotFoundException e) {
            return false;
        }
    }
}
