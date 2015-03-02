import java.util.ArrayList;
import java.util.List;

/**
 * Internal organization of a Decision Tree node.
 * @author Bob (Robert) Wagner
 */
public class DecTreeNode {
	
	protected String label;			//node label
	protected String attribute;		//node attribute
	protected String parentAttributeValue; 	//attribute value of parent; if root, set to "ROOT"
	protected boolean isTerminal;		//terminal node boolean
	protected List<DecTreeNode> children;	//list of children

	//constructor initializing node label, attribute, attribute of parent, & terminality
	public DecTreeNode(String _label, String _attribute, String _parentAttributeValue, boolean _isTerminal) {
		label = _label;
		attribute = _attribute;
		parentAttributeValue = _parentAttributeValue;
		isTerminal = _isTerminal;
		if (isTerminal) {
			children = null;
		} else {
			children = new ArrayList<DecTreeNode>();
		}
	}
	
	//copy constructor initializing decision tree node from an existing one
	public DecTreeNode(DecTreeNode copy) {
		label = copy.label;
		attribute = copy.attribute;
		parentAttributeValue = copy.parentAttributeValue;
		isTerminal = copy.isTerminal;
		children = copy.children;
	}

	/**
	 * Add child to the node.
	 * 
	 * For printing to be consistent, children should be added
	 * in order of the attribute values as specified in the
	 * dataset.
	 * @param child
	 */
	public void addChild(DecTreeNode child) {
		if (children != null) {
			children.add(child);
		}
	}
	
	/**
	 * Recursively prints the subtree of the node
	 * with each line prefixed by k * 4 blank spaces.
	 * @param k	the indentation amount, seeded with 0
	 */
	public void print(int k) {
		//use StringBuilder for better time complexity ( O(N) vs O(N^2) )
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < k; i++) {
			sb.append("    ");
		}
		sb.append(parentAttributeValue);
		if (terminal) {
			sb.append(" (" + label + ")");
			System.out.println(sb.toString());
		} else {
			sb.append(" {" + attribute + "?}");
			System.out.println(sb.toString());
			for(DecTreeNode child : children) {
				child.print(k+1);
			}
		}
	}
	
}
