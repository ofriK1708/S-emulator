
package engine.generated_2;

import jakarta.xml.bind.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type</p>.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 *
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{}S-Instruction" maxOccurs="unbounded"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "sInstruction"
})
@XmlRootElement(name = "S-Instructions")
public class SInstructions {

    @XmlElement(name = "S-Instruction", required = true)
    protected List<SInstruction> sInstruction;

    /**
     * Gets the value of the sInstruction property.
     *
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sInstruction property.</p>
     *
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSInstruction().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SInstruction }
     * </p>
     *
     * @return The value of the sInstruction property.
     */
    public @NotNull List<SInstruction> getSInstruction() {
        if (sInstruction == null) {
            sInstruction = new ArrayList<>();
        }
        return this.sInstruction;
    }

}
