
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
 *         <element ref="{}S-Function" maxOccurs="unbounded"/>
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
        "sFunction"
})
@XmlRootElement(name = "S-Functions")
public class SFunctions {

    @XmlElement(name = "S-Function", required = true)
    protected List<SFunction> sFunction;

    /**
     * Gets the value of the sFunction property.
     *
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sFunction property.</p>
     *
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSFunction().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SFunction }
     * </p>
     *
     * @return The value of the sFunction property.
     */
    public @NotNull List<SFunction> getSFunction() {
        if (sFunction == null) {
            sFunction = new ArrayList<>();
        }
        return this.sFunction;
    }

}
