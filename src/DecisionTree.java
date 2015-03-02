import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/* This class models a Decision Tree, with nodes
 * that are generated with specific attribute
 * branches--based upon learning from a training
 * data set. This model is then tuned using a
 * small partition of the original data set
 * not included in the original training.
 *
 * The accuracy of the resulting Decision Tree
 * structure can be tested from a small section of
 * the original data not included in either the
 * training or tuning processes.
 *
 * @author Bob (Robert) Wagner
 */
public class DecisionTree {
	
	private DataSet train = null;	// Training Data Set
	private DataSet tune = null;	// Tuning Data Set
	private DataSet test = null;	// Testing Data Set
	private DecTreeNode root = null;// Root node
	
	List<DecTreeNode> rtn = new ArrayList<DecTreeNode>();

	/* Constructor that initalizes the training, tuning, and testing
	 * data sets for this Decision Tree instance.
	 * 
	 * @param _train	the training data set
	 * @param _tune		the tuning data set
	 * @param _test	 	the testing data set
	 */
	public DecisionTree(DataSet _train, DataSet _tune, DataSet _test) {
		train = _train;
		tune = _tune;
		test = _test;
	}
	
	/* Calculate & print the Information Gain of each possible question at root node.
	 * (see http://en.wikipedia.org/wiki/Information_gain_in_decision_trees)
	 * IG(T,a) = H(T) - H(T|a), where H is the Information Entropy
	 * 	(see http://en.wikipedia.org/wiki/Entropy_(information_theory) )
	 */
	public void printInfoGain() {
		//Iterate through training data and tally the number of edible and poisonous labels
		int eCount = 0, pCount = 0;
		for(int i = 0; i < this.train.instances.size(); i++) {
			if(this.train.instances.get(i).label.equals(this.train.labels[0])) eCount++;
			else pCount++;
		}
		
		//Use this number to determine whether we are looking for H(e[dible]) or H(p[oisonous])
		String majorityLabel;
		int majorityCount, minorityCount;
		if(eCount < pCount) {
			majorityLabel = this.train.labels[1];
			majorityCount = pCount;
			minorityCount = eCount;
		} else { //tie or eCount > pCount, so choose first listed class label
			majorityLabel = this.train.labels[0];
			majorityCount = eCount;
			minorityCount = pCount;
		}
		
		//Calculate H(e) or H(p)
		double pMaj = (double) majorityCount/this.train.instances.size();
		double pMin = (double) minorityCount/this.train.instances.size();
		double hY = -(double) (pMaj)*((double) Math.log(pMaj)/Math.log(2))
				     -((double) (pMin)*((double) Math.log(pMin)/Math.log(2)));
		
		double infoGain;
		for(int attr = 0; attr < this.train.attr_name.length; attr++) {
			//Print I(Y;X) = H(Y) - H(Y|X)	
			infoGain = hY - findhYX(attr, attr, majorityLabel, this.train.instances);
			System.out.println(this.train.attr_name[attr] + ": info gain = " 
					+ new BigDecimal(String.valueOf(infoGain)).setScale(3, BigDecimal.ROUND_FLOOR));
		}
		
	}

	// Build a decision tree given only a training set.
	public void buildTree() {
		buildTree(null, this.train.instances, this.train.attr_name);
	}
	
	/* Build a Decision Tree using majority vote to determine label
	 * @param node		the current Decision Tree node, seed with root, recurse on subtree traversal
	 * @param examples	the list of data examples
	 * @param questions	the list of questions asked
	 */
	public void buildTree(DecTreeNode node, List<Instance> examples, String[] questions) {

		//Iterate through training data and tally the number of edible and poisonous (mushroom) labels
		int eCount = 0, pCount = 0;
		for(int i = 0; i < examples.size(); i++) {
			if(examples.get(i).label.equals(this.train.labels[0])) eCount++; 
			else pCount++;
		}
		
		//Use this number to determine whether we are looking for H(e[dible]) or H(p[oisonous])
		String majorityLabel;
		int majorityCount, minorityCount;
		if(eCount < pCount) {
			majorityLabel = this.train.labels[1];
			majorityCount = pCount;
			minorityCount = eCount;
		} else { //tie or eCount > pCount, so choose first listed class label by default
			majorityLabel = this.train.labels[0];
			majorityCount = eCount;
			minorityCount = pCount;
		}
		
		//Calculate H(e) or H(p)
		double pMaj = (double) majorityCount/examples.size();
		double pMin = (double) minorityCount/examples.size();
		double hY = -(double) (pMaj)*((double) Math.log(pMaj)/Math.log(2))
				     -((double) (pMin)*((double) Math.log(pMin)/Math.log(2)));
		
		if(examples.isEmpty()) {
			//Training examples are exhausted
			node.label = majorityLabel;
			node.terminal = true;
			return;
		}
		// test if all examples have the same label
		boolean allSame = true;
		for(int w = 0; (w < examples.size()) && allSame; w++) {
			if(!examples.get(w).label.equals(examples.get(0).label)) {
				allSame = false;
			}
		}
		//if all examples have the same label
		if(allSame) { 
			node.label = examples.get(0).label;
			node.terminal = true;
			return;
		}
		//if no more questions exist
		if(questions.length == 0) {
			node.label = majorityLabel;
			node.terminal = true;
			return;
		}	
		
		//find index of best question
		int indexOfBQ = findBestQuestion(questions, hY, majorityLabel, examples);
		
		//initial case (root)
		if(node == null) {
			//construct root node & build tree
			this.root = new DecTreeNode(null, questions[indexOfBQ], "ROOT", false); 
			buildTree(root, examples, questions);					
		} else {
			node.attribute = questions[indexOfBQ];
			boolean found = false;
			int newIndex = 0;
			for(int g = 0; (g < this.train.attr_val.length) && !found; g++) {
				if(questions[indexOfBQ].equals(this.train.attr_name[g])) {
					found = true;
					newIndex = g;
				}
			}
			//iterate over possible answers to best question
			for(int j = 0; j < this.train.attr_val[newIndex].length; j++) { 
				DecTreeNode newNode = new DecTreeNode(null, null, this.train.attr_val[newIndex][j], false);
				node.addChild(newNode);
				buildTree(newNode, selectInstances(examples, 
					this.train.attr_val[newIndex][j], indexOfBQ),
					eliminateQuestion(questions, indexOfBQ));
			}
		}
						
	}
			
	
	private boolean pruneNode(DecTreeNode root, DecTreeNode node, 
		List<String> attributeList, List<Integer> attributeNum) {
		
		boolean allTerminal = true;
		for(int o = 0; o < node.children.size(); o++) {
			if(!node.children.get(o).terminal) {
				allTerminal = false;
				for(int u = 0; u < this.train.attr_name.length; u++) {
					if(node.attribute.equals(this.train.attr_name[u])) {
						attributeNum.add(u);
					}
				}
				attributeList.add(node.attribute);
				DecTreeNode newRoot = new DecTreeNode(root);
				pruneNode(newRoot, node.children.get(o), attributeList, attributeNum);
			}
		}
			
		if(allTerminal) {
			node.terminal = true;
			List<Instance> ins = new ArrayList<Instance>();
			for(int g = 0; g < this.train.instances.size(); g++) {
				boolean hasAllAttributes = true;
				for(int h = 0; h < attributeNum.size(); h++) {
					if(!this.train.instances.get(g).attributes.get(h).equals(attributeList.get(h))) {
						hasAllAttributes = false; 
					}
				}
				//if it has all attributes, add it
				if(hasAllAttributes) {
					ins.add(this.train.instances.get(g)); 
				}
			}
			
			int eCount = 0, pCount = 0;
			for(int i = 0; i < ins.size(); i++) {
				if(ins.get(i).label.equals(this.train.labels[0])) eCount++;
				else pCount++;
			}
			if(eCount < pCount) {
				node.label = this.train.labels[1];
			} else { //tie or eCount > pCount, so choose first listed class label by default
				node.label = this.train.labels[0];
			}
			node.children = null;
			rtn.add(root);
		}
		return false;		
	}
	
	/* Build a decision tree given a training set, then prune it using a tuning set. */
	public void buildPrunedTree() {

		//initial build of tree using training set
		this.buildTree(); 
		
		DecTreeNode bestRoot = new DecTreeNode(root);
		double bestAccuracy = 0.0;
		boolean updatedRoot = false;
		
		do {
			//update best accuracy
			bestAccuracy = determineAccuracy(this.tune.instances, classifyTune(bestRoot));
			
			//reset list of pruned trees
			rtn.clear(); 
			
			//reset status that root has been updated
			updatedRoot = false; 
			
			DecTreeNode tmp = new DecTreeNode(bestRoot);
			
			//add pruned list of trees to rtn
			pruneNode(tmp, tmp, new ArrayList<String>(), new ArrayList<Integer>()); 
			
			//if more accurate tree exists in pruned set, update bestRoot
			for(int z = 0; z < rtn.size(); z++) {
				double thisAccuracy = determineAccuracy(this.tune.instances, classifyTune(rtn.get(z)));
			    	if(thisAccuracy > bestAccuracy) {
					bestAccuracy = thisAccuracy;
					bestRoot = rtn.get(z);
					updatedRoot = true;
				}
			}
		} while (updatedRoot);
		
		//finally set best pruned root node to this.root
		root = bestRoot;
	}
	
	/* Recursively finds the maximum depth of the constructed
	 * Decision Tree using DFS.
	 * @param node	the node used to recursively traverse the Decision Tree, seed with root
	 * @return 	the maximum depth of the constructed Decision Tree
	 */
	private int getMaxDepth(DecTreeNode node) {
		int depth = 0;
	    	for (int a = 0; a < node.children.size(); a++) {
	    		depth = Math.max(depth, getMaxDepth(node.children.get(a)));
		}
	    	return ++depth;
	}
	
	/* Determines the accuracy of the Decision Tree by
	 * comparing it against the test set.
	 * @param set
	 * @param results
	 * @return 		accuracy; a double as a percentage of correct vs total
	 */
	private double determineAccuracy(List<Instance> set, String[] results) {
		int correct = 0, total = set.size();
		for(int i = 0; i < total; i++) {
			if(set.get(i).label.equals(results[i])) {
				correct++; 
			}
		}
		return (double) correct/total; 
	}
	
	/* Evaluates the learned decision tree on a tune set.
	 * @return 	the label predictions for each test instance 
	 * 		according to the order in data set list.
	 */
	public String[] classifyTune(DecTreeNode root) {
		
		String[] predicted = new String[this.tune.instances.size()];
		for(int x = 0; x < this.tune.instances.size(); x ++) {
			predicted[x] = traverse(root, this.tune.instances.get(x));
		}
		return predicted;
	}

	private String traverse(DecTreeNode node, Instance instance) {
	
		if(node.terminal) {
			return node.label; 
		} else {
			boolean found = false;
			int indexOfAttr = 0;
			for(int y = 0; (y < this.test.attr_name.length) && !found; y++) { //find index of Attribute
				if(this.test.attr_name[y].equals(node.attribute)) {
					found = true;
					indexOfAttr = y;
				}
			}
			
			found = false;
			DecTreeNode nextNode = new DecTreeNode(null, null, null, false);
			for(int y = 0; (y < node.children.size()) && !found; y++) {
				if(node.children.get(y).parentAttributeValue.equals(instance.attributes.get(indexOfAttr))) {
					found = true;
					nextNode = node.children.get(y);
				}
			}
			return traverse(nextNode, instance);
		}
		
	}

	public void print() {
		root.print(0);
	}
	
	private double findhYX(int indexOfAttributeInTrain, int indexOfAttributeInExamples, 
		String majorityLabel, List<Instance> examples) {
		
		//calculate H(Y|X) for given attribute
		int numValOfAttr = this.train.attr_val[indexOfAttributeInTrain].length;
		String[] attributeVals = this.train.attr_val[indexOfAttributeInTrain];
		int trainSetSize = examples.size();
		int[] attributeTally = new int[numValOfAttr];
		int[] attributeNumMaj = new int[numValOfAttr];
		
		for(int k = 0; k < trainSetSize; k++) {
			for(int a = 0; a < numValOfAttr; a++) {
				if(examples.get(k).attributes.get(indexOfAttributeInExamples).equals(attributeVals[a])) {
					attributeTally[a]++;
					if(examples.get(k).label.equals(majorityLabel)) {
						attributeNumMaj[a]++;
					}
				}
			}
		}
		
		double hYX = 0.0;
		for(int k = 0; k < numValOfAttr; k++) {
			
			if(attributeTally[k] != 0) {
				if (attributeNumMaj[k] == attributeTally[k]) {
					hYX += (double) ((double) attributeTally[k]/trainSetSize)*
						   (-((double) attributeNumMaj[k]/attributeTally[k])*((double) (Math.log(attributeNumMaj[k]) - Math.log(attributeTally[k]))/(Math.log(2))));		
				} else if (attributeNumMaj[k] == 0) {
					hYX += (double) ((double) attributeTally[k]/trainSetSize)*
						   (-(((double) (attributeTally[k]-attributeNumMaj[k])/attributeTally[k])*((double) (Math.log(attributeTally[k]-attributeNumMaj[k])-Math.log(attributeTally[k]))/(Math.log(2)))));
				} else {
					hYX += (double) ((double) attributeTally[k]/trainSetSize)*
						       ((-((double) attributeNumMaj[k]/attributeTally[k])*((double) (Math.log(attributeNumMaj[k]) - Math.log(attributeTally[k]))/(Math.log(2))))
						       -(((double) (attributeTally[k]-attributeNumMaj[k])/attributeTally[k])*((double) (Math.log(attributeTally[k]-attributeNumMaj[k])-Math.log(attributeTally[k]))/(Math.log(2)))));
				}
			}
		}
		return hYX;
		
	}
	
	
	private int findBestQuestion(String[] questions, double hY, String majorityLabel, List<Instance> examples) {
		
		double tmp = 0.0, infoGain = 0.0;
		int index = 0;
		for(int attr = 0; attr < questions.length; attr++) {
			
			//find attribute index of questions in this.train.attr_name
			boolean found = false;
			int newIndex = 0;
			for(int g = 0; g < this.train.attr_name.length && !found; g++) {
				if(this.train.attr_name[g].equals(questions[attr])) {
					found = true;
					newIndex = g;
				}
			}
			
			tmp = hY - findhYX(newIndex, attr, majorityLabel, examples);
			if(tmp > infoGain) { //find index of highest info gain
				infoGain = tmp; 
				index = attr;
			}			
			
		}
		return index;		
		
	}
	
	private List<Instance> selectInstances(List<Instance> _examples, String attr_val, int attr_idx) {
		
		List<Instance> newExamples = new ArrayList<Instance>();
		for(int count = 0; count < _examples.size(); count++) {
			//add all examples that have specified attribute
			if(_examples.get(count).attributes.get(attr_idx).equals(attr_val)) { 
				newExamples.add(_examples.get(count));
			}
		}
		return newExamples;
		
	}
	
	
	private String[] eliminateQuestion(String[] questions, int indexOfBQ) {
		
		String[] newQuestions = new String[questions.length-1];
		if(questions[questions.length-1].equals(questions[indexOfBQ])) {
			for(int z = 0; z < questions.length-1; z++) { 
				newQuestions[z] = questions[z]; 
			}
		} else {
			int newIndex = 0;
			for(int h = 0; h < questions.length; h++) {
				newQuestions[newIndex] = questions[h];
				if(!questions[h].equals(questions[indexOfBQ])) {
					newIndex++; 
				}
			}
		}
		return newQuestions;
		
	}
	
}
