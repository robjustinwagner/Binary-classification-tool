import java.util.ArrayList;
import java.util.List;

/* This class holds information for particular instance
 * of our data set, including a label and list of attributes
 * associated with the instance. These instances are collected
 * in DataSet.java.
 *
 * @author Bob (Robert) Wagner
 */
public class Instance {
	
	//label of the data instance
	private String label;
	//list of attributes associated with the data instance
	private List<String> attributes = new ArrayList<String>();
	
	/* Gets the label value of this data instance.
	 * @return	the label value
	 */
	public String getLabel() {
		return label;
	}
	
	/* Sets the label value for this data instance.
	 * @param _label	the label value to set
	 */
	public void setLabel(String _label) {
		label = _label;
	}
	
	/* Gets the list of attributes associated with this data instance.
	 * @return	the list of attributes
	 */
	public List<String> getAttributes() {
		return attributes;
	}
	
	/* Adds an attribute value for this data instance.
	 * @param attributeToAdd	the attribute to add.
	 */
	public void addAttribute(String attributeToAdd) {
		attributes.add(attributeToAdd);
	}
	
}
