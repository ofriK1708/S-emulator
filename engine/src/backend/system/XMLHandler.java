package backend.system;

import backend.system.generated.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLHandler {
    private final Unmarshaller unmarshaller;

    public XMLHandler() throws JAXBException {
        String jaxbGeneratedPackageLoc = "backend.system.generated";
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbGeneratedPackageLoc);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public SProgram unmarshallForm(Path xmlPath) throws JAXBException, IOException
    {
        if (!xmlPath.getFileName().toString().endsWith(".xml"))
        {
            throw new IOException("The provided path does not point to an XML file: "
                    + xmlPath);
        }
        InputStream xmlFile = Files.newInputStream(xmlPath);
        return (SProgram) unmarshaller.unmarshal(xmlFile);
    }

//    public boolean validateXMLStructure(Path xmlPath) {
//        try {
//            unmarshallForm(xmlPath);
//            return true;
//        } catch (JAXBException | IOException e) {
//            return false;
//        }
//    }
}
