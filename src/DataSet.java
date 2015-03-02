import java.util.ArrayList;
import java.util.List;

/* This class organizes the information of a data set 
 * into simple, constant-access data structures.
 * 
 * @author Bob (Robert) Wagner
 */
public class DataSet {

	private List<Instance> instances = new ArrayList<Instance>(); // ordered list of instances

	private String [] labels = null;  	// instance labels
	private String [] attr_name = null; 	// attribute names
	private String [][] attr_val = null;    // candidate values of each attribute
	
	/* Adds an instance to the data set collection,
	 * populating its label value and associated
	 * attribute list.
	 * @param line	a line of the data file read in
	 *		with format:
	 *			label,attr_1,attr_2,...		
	 */
	public void addInstance(String line) {
		Instance instanceToAdd = new Instance();
		//parse line
		String[] splitLine = line.split(",");
		instanceToAdd.setLabel(splitLine[0]);
		for(int i = 1; i < splitLine.length; i++) {
			instanceToAdd.addAttribute(splitLine[i]);
		}
		//add to list of data instances
		instances.add(instanceToAdd);
	}
	
	/* Adds a specific attribute instance to the data set collection,
	 * populating its label value and associated
	 * attribute list.
	 * @param line	a line of the data file read in
	 *		with format:
	 *			label,attr_1,attr_2,...
	 * @param idx	the index of the attribute in
	 *		the specified line
	 */
	public void addAttribute(String line, int idx) {
		String[] splitLine = line.split(" ");
		attr_name[idx] = splitLine[0];
		attr_val[idx] = splitLine[1].split(",").clone();
	}
}
