package backend.engine.loader;

import backend.engine.Command;
import backend.engine.CommandType;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main loader class for loading S-EMULATOR programs from XML files
 */
public class XMLCommandLoader implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommandFactory commandFactory;
    private transient Schema schema; // transient because Schema is not serializable

    public XMLCommandLoader() {
        this.commandFactory = new CommandFactory();
        initializeSchema();
    }

    private void initializeSchema() {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Try to load from resources folder first, then from current directory
            InputStream schemaStream = getClass().getResourceAsStream("/S-Emulator-v1.xsd");
            if (schemaStream == null) {
                // Try to load from current directory
                File schemaFile = new File("S-Emulator-v1.xsd");
                if (schemaFile.exists()) {
                    this.schema = schemaFactory.newSchema(schemaFile);
                } else {
                    System.err.println("Warning: S-Emulator-v1.xsd not found in resources or current directory");
                    this.schema = null;
                }
            } else {
                this.schema = schemaFactory.newSchema(new javax.xml.transform.stream.StreamSource(schemaStream));
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load XSD schema for validation: " + e.getMessage());
            this.schema = null;
        }
    }

    /**
     * Loads a program from an XML file
     * @param xmlFilePath Path to the XML file (can be absolute or relative to current directory)
     * @return ProgramData containing the loaded program
     * @throws XMLLoaderException if loading fails
     */
    public ProgramData loadProgramFromXML(String xmlFilePath) throws XMLLoaderException {
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                throw new XMLLoaderException("XML file not found: " + xmlFilePath +
                        " (Current directory: " + System.getProperty("user.dir") + ")");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            if (schema != null) {
                factory.setSchema(schema);
                System.out.println("Using XSD validation");
            } else {
                System.out.println("Warning: Running without XSD validation");
            }

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            System.out.println("Successfully loaded XML file: " + xmlFile.getAbsolutePath());
            return parseProgramFromDocument(document);

        } catch (Exception e) {
            throw new XMLLoaderException("Failed to load program from XML: " + xmlFilePath, e);
        }
    }

    /**
     * Loads a program from an XML input stream
     */
    public ProgramData loadProgramFromStream(InputStream xmlStream) throws XMLLoaderException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            if (schema != null) {
                factory.setSchema(schema);
            }

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlStream);
            document.getDocumentElement().normalize();

            return parseProgramFromDocument(document);

        } catch (Exception e) {
            throw new XMLLoaderException("Failed to load program from XML stream", e);
        }
    }

    private ProgramData parseProgramFromDocument(Document document) throws XMLLoaderException {
        Element rootElement = document.getDocumentElement();

        if (!"S-Program".equals(rootElement.getNodeName())) {
            throw new XMLLoaderException("Root element must be 'S-Program'");
        }

        String programName = rootElement.getAttribute("name");
        if (programName.isEmpty()) {
            throw new XMLLoaderException("Program name is required");
        }

        NodeList instructionsNodeList = rootElement.getElementsByTagName("S-Instructions");
        if (instructionsNodeList.getLength() != 1) {
            throw new XMLLoaderException("Exactly one S-Instructions element is required");
        }

        Element instructionsElement = (Element) instructionsNodeList.item(0);
        List<InstructionData> instructions = parseInstructions(instructionsElement);

        // Build label map
        Map<String, Integer> labelMap = buildLabelMap(instructions);

        return new ProgramData(programName, instructions, labelMap);
    }

    private List<InstructionData> parseInstructions(Element instructionsElement) throws XMLLoaderException {
        NodeList instructionNodes = instructionsElement.getElementsByTagName("S-Instruction");
        List<InstructionData> instructions = new ArrayList<>();

        for (int i = 0; i < instructionNodes.getLength(); i++) {
            Element instructionElement = (Element) instructionNodes.item(i);
            instructions.add(parseInstruction(instructionElement));
        }

        return instructions;
    }

    private InstructionData parseInstruction(Element instructionElement) throws XMLLoaderException {
        String commandName = instructionElement.getAttribute("name");
        String commandType = instructionElement.getAttribute("type");

        if (commandName.isEmpty() || commandType.isEmpty()) {
            throw new XMLLoaderException("Command name and type are required");
        }

        // Parse variable
        NodeList variableNodes = instructionElement.getElementsByTagName("S-Variable");
        if (variableNodes.getLength() != 1) {
            throw new XMLLoaderException("Exactly one S-Variable element is required per instruction");
        }
        String variable = variableNodes.item(0).getTextContent();

        // Parse optional label
        Optional<String> label = Optional.empty();
        NodeList labelNodes = instructionElement.getElementsByTagName("S-Label");
        if (labelNodes.getLength() == 1) {
            label = Optional.of(labelNodes.item(0).getTextContent());
        } else if (labelNodes.getLength() > 1) {
            throw new XMLLoaderException("At most one S-Label element is allowed per instruction");
        }

        // Parse arguments
        List<InstructionData.ArgumentData> arguments = parseArguments(instructionElement);

        return new InstructionData(commandName, commandType, variable, label, arguments);
    }

    private List<InstructionData.ArgumentData> parseArguments(Element instructionElement) throws XMLLoaderException {
        NodeList argumentsNodes = instructionElement.getElementsByTagName("S-Instruction-Arguments");

        if (argumentsNodes.getLength() == 0) {
            return new ArrayList<>();
        }

        if (argumentsNodes.getLength() > 1) {
            throw new XMLLoaderException("At most one S-Instruction-Arguments element is allowed per instruction");
        }

        Element argumentsElement = (Element) argumentsNodes.item(0);
        NodeList argumentNodes = argumentsElement.getElementsByTagName("S-Instruction-Argument");

        List<InstructionData.ArgumentData> arguments = new ArrayList<>();
        for (int i = 0; i < argumentNodes.getLength(); i++) {
            Element argumentElement = (Element) argumentNodes.item(i);
            String name = argumentElement.getAttribute("name");
            String value = argumentElement.getAttribute("value");

            if (name.isEmpty() || value.isEmpty()) {
                throw new XMLLoaderException("Argument name and value are required");
            }

            arguments.add(new InstructionData.ArgumentData(name, value));
        }

        return arguments;
    }

    private Map<String, Integer> buildLabelMap(List<InstructionData> instructions) {
        Map<String, Integer> labelMap = new HashMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            InstructionData instruction = instructions.get(i);
            if (instruction.getLabel().isPresent()) {
                String label = instruction.getLabel().get();
                if (labelMap.containsKey(label)) {
                    throw new IllegalArgumentException("Duplicate label: " + label);
                }
                labelMap.put(label, i);
            }
        }

        return labelMap;
    }

    /**
     * Creates Command instances from loaded program data
     */
    public List<Command> createCommands(ProgramData programData) throws XMLLoaderException {
        return programData.getInstructions().stream()
                .map(this::createCommandFromInstruction)
                .collect(Collectors.toList());
    }

    private Command createCommandFromInstruction(InstructionData instructionData) {
        CommandType type = parseCommandType(instructionData.getCommandType());
        return commandFactory.createCommand(instructionData.getCommandName(), type);
    }

    private CommandType parseCommandType(String typeString) {
        switch (typeString.toLowerCase()) {
            case "basic":
                return CommandType.BASIC;
            case "synthetic":
                return CommandType.SYNTHETIC;
            default:
                throw new IllegalArgumentException("Unknown command type: " + typeString);
        }
    }

    // Serialization support
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeSchema(); // Reinitialize transient schema after deserialization
    }

    // Helper method for creating indexed streams (replacing the problematic enumerate)
}