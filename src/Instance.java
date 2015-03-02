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
	
	public String label;			//label of the data instance
	public List<String> attributes = null;	//list of attributs associated with the data instance

	/* Adds an attribute value for this data instance.
	 * @param attributeToAdd	the attribute to add.
	 */
	public void addAttribute(String attributeToAdd) {
		if (attributes == null) {
			attributes = new ArrayList<String>();
		}
		attributes.add(attributeToAdd);
	}
	
	/* Sets the label value for this data instance.
	 * @_label	the label value to set
	 */
	public void setLabel(String _label) {
		label = _label;
	}
	
}
