
package engine.generated;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}S-Instruction-Argument" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sInstructionArgument"
})
@XmlRootElement(name = "S-Instruction-Arguments")
public class SInstructionArguments {

    @XmlElement(name = "S-Instruction-Argument", required = true)
    protected List<SInstructionArgument> sInstructionArgument;

    /**
     * Gets the value of the sInstructionArgument property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the sInstructionArgument property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSInstructionArgument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SInstructionArgument }
     * 
     * 
     */
    public List<SInstructionArgument> getSInstructionArgument() {
        if (sInstructionArgument == null) {
            sInstructionArgument = new ArrayList<SInstructionArgument>();
        }
        return this.sInstructionArgument;
    }

}
