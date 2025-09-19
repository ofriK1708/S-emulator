
package engine.generated_1;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated_1 in the generated_1 package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SVariable_QNAME = new QName("", "S-Variable");
    private final static QName _SLabel_QNAME = new QName("", "S-Label");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated_1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SProgram }
     * 
     */
    public @NotNull SProgram createSProgram() {
        return new SProgram();
    }

    /**
     * Create an instance of {@link SInstructions }
     * 
     */
    public @NotNull SInstructions createSInstructions() {
        return new SInstructions();
    }

    /**
     * Create an instance of {@link SInstruction }
     * 
     */
    public @NotNull SInstruction createSInstruction() {
        return new SInstruction();
    }

    /**
     * Create an instance of {@link SInstructionArguments }
     * 
     */
    public @NotNull SInstructionArguments createSInstructionArguments() {
        return new SInstructionArguments();
    }

    /**
     * Create an instance of {@link SInstructionArgument }
     * 
     */
    public @NotNull SInstructionArgument createSInstructionArgument() {
        return new SInstructionArgument();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "S-Variable")
    public @NotNull JAXBElement<String> createSVariable(String value) {
        return new JAXBElement<String>(_SVariable_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "S-Label")
    public @NotNull JAXBElement<String> createSLabel(String value) {
        return new JAXBElement<String>(_SLabel_QNAME, String.class, null, value);
    }

}
