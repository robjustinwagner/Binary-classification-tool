import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a framework for accessing a decision tree.
 * Put your code in constructor, printInfoGain(), buildTree and buildPrunedTree()
 * You can add your own help functions or variables in this class 
 */
public class DecisionTree {
	/**
	 * training data set, pruning data set and testing data set
	 */
	private DataSet train = null;		// Training Data Set
	private DataSet tune = null;		// Tuning Data Set
	private DataSet test = null;		// Testing Data Set
	private DecTreeNode root;
	List<DecTreeNode> rtn = new ArrayList<DecTreeNode>();

	/**
	 * Constructor
	 * 
	 * @param train  
	 * @param tune
	 * @param test
	 */
	DecisionTree(DataSet train, DataSet tune, DataSet test) {
		this.train = train;
		this.tune = tune;
		this.test = test;
		this.root = null;
	}
	
	/**
	 * print information gain of each possible question at root node.
	 * 
	 */
	public void printInfoGain() {
		//Iterate through training data and tally the number of edible and poisonous labels
		int eCount = 0, pCount = 0;
		for(int i = 0; i < this.train.instances.size(); i++) {
			
			if(this.train.instances.get(i).label.equals(this.train.labels[0])) { eCount++; } 
			else { pCount++; }
			
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
	
	/**
	 * Build a decision tree given only a training set.
	 * 
	 */
	public void buildTree() { //initial call
		buildTree(null, this.train.instances, this.train.attr_name);
	}
	
	//uses majority vote to determine label
	public void buildTree(DecTreeNode node, List<Instance> examples, String[] questions) {

		//Iterate through training data and tally the number of edible and poisonous labels
		int eCount = 0, pCount = 0;
		for(int i = 0; i < examples.size(); i++) {
			
			if(examples.get(i).label.equals(this.train.labels[0])) { eCount++; } 
			else { pCount++; }
			
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
		
		if(examples.isEmpty()) { //Training examples are exhausted
			node.label = majorityLabel;
			node.terminal = true;
			return;
		}
		boolean allSame = true;
		for(int w = 0; (w < examples.size()) && allSame; w++) { // test if all examples have the same label
			if(!examples.get(w).label.equals(examples.get(0).label)) { allSame = false;	}
		}
		if(allSame) { //if all examples have the same label
			node.label = examples.get(0).label;
			node.terminal = true;
			return;
		}
		if(questions.length == 0) { //if no more questions
			node.label = majorityLabel;
			node.terminal = true;
			return;
		}	
		int indexOfBQ = findBestQuestion(questions, hY, majorityLabel, examples); //find best question index
		if(node == null) { //initial case (root)
			this.root = new DecTreeNode(null, questions[indexOfBQ], "ROOT", false); //construct root node
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
			for(int j = 0; j < this.train.attr_val[newIndex].length; j++) { //iterate over possible answers to best question
				DecTreeNode newNode = new DecTreeNode(null, null, this.train.attr_val[newIndex][j], false);
				node.addChild(newNode);
				buildTree(newNode, selectInstances(examples, this.train.attr_val[newIndex][j], indexOfBQ), eliminateQuestion(questions, indexOfBQ));
			}
		}
						
	}
			
	private boolean pruneNode(DecTreeNode root, DecTreeNode node, List<String> attributeList, List<Integer> attributeNum) {
		
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
			//NEED TO MAKE COPY OF ROOT NODE NOT CURRENT, JUST MODIFY CURRENT
			node.terminal = true;
			List<Instance> ins = new ArrayList<Instance>();
			for(int g = 0; g < this.train.instances.size(); g++) {
				boolean hasAllAttributes = true;
				for(int h = 0; h < attributeNum.size(); h++) {
					if(!this.train.instances.get(g).attributes.get(h).equals(attributeList.get(h))) { hasAllAttributes = false; }
				}
				if(hasAllAttributes) { ins.add(this.train.instances.get(g)); } //if it has all attributes, add it
			}
			int eCount = 0, pCount = 0;
			for(int i = 0; i < ins.size(); i++) {
				if(ins.get(i).label.equals(this.train.labels[0])) { eCount++; } 
				else { pCount++; }
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
	

	/**
	 * Build a decision tree given a training set then prune it using a tuning set.
	 * 
	 */
	public void buildPrunedTree() {

		this.buildTree(); //initial build of tree using training set
		DecTreeNode bestRoot = new DecTreeNode(root);
		double bestAccuracy = 0.0;
		boolean updatedRoot = false;
		do {
			bestAccuracy = determineAccuracy(this.tune.instances, classify2(bestRoot)); //update best accuracy
			rtn.clear(); //reset list of pruned trees
			updatedRoot = false; //reset status that root has been updated
			
			DecTreeNode tmp = new DecTreeNode(bestRoot);
			pruneNode(tmp, tmp, new ArrayList<String>(), new ArrayList<Integer>()); //add pruned list of trees to rtn
			
			//if more accurate tree exists in pruned set, update bestRoot
			for(int z = 0; z < rtn.size(); z++) {
				double thisAccuracy = determineAccuracy(this.tune.instances, classify2(rtn.get(z)));
			  //TODO: ALLOW DEPTH CHECK (which is working properly) WHEN I GET PRUNED TREES WORKING 
			  //if(thisAccuracy >= bestAccuracy) {
			    if(thisAccuracy > bestAccuracy) {
				/*	if(thisAccuracy == bestAccuracy) {
						if(getMaxDepth(bestRoot) > getMaxDepth(rtn.get(z))) {
							bestRoot = rtn.get(z);
							updatedRoot = true;
						}
					} else { */
						bestAccuracy = thisAccuracy;
						bestRoot = rtn.get(z);
						updatedRoot = true;
				//}
					
				}
			}
			
		} while (updatedRoot);
		
		root = bestRoot; //finally set best pruned root node to this.root
		
	}
	
	private int getMaxDepth(DecTreeNode node) {
		int depth = 0;
	    for (int a = 0; a < node.children.size(); a++) {
	    	depth = Math.max(depth, getMaxDepth(node.children.get(a)));
	    }
	    return depth + 1;
	}

	public String[] classify2(DecTreeNode root) {
		
		String[] predicted = new String[this.tune.instances.size()];
		for(int x = 0; x < this.tune.instances.size(); x ++) {
			predicted[x] = traverse(root, this.tune.instances.get(x));
		}
		return predicted;
	}
	private double determineAccuracy(List<Instance> set, String[] results) {
		int correct = 0, total = set.size();
		for(int i = 0; i < total; i ++) {
			if(set.get(i).label.equals(results[i])) { correct ++; }
		}
		return (double) correct/total; 
	}

	
  /**
   * Evaluates the learned decision tree on a test set.
   * @return the label predictions for each test instance 
   * 	according to the order in data set list
   */
	public String[] classify() {
		
		String[] predicted = new String[this.test.instances.size()];
		for(int x = 0; x < this.test.instances.size(); x ++) {
			predicted[x] = traverse(root, this.test.instances.get(x));
		}
		return predicted;
	}

	private String traverse(DecTreeNode node, Instance instance) {
	
		if(node.terminal) { return node.label; }
		else {
		
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

	/**
	 * Prints the tree in specified format. It is recommended, but not
	 * necessary, that you use the print method of DecTreeNode.
	 * 
	 * Example:
	 * Root {odor?}
     *     a (e)
     *     m (e)
   	 *	   n {habitat?}
     *         g (e)
     *  	   l (e)
     *	   p (p)
   	 *	   s (e)
	 *         
	 */
	public void print() {
		root.print(0);
	}
	
	
	private double findhYX(int indexOfAttributeInTrain, int indexOfAttributeInExamples, String majorityLabel, List<Instance> examples) {
		
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
		for(int count = 0; count < _examples.size(); count++) { //add all examples that have specified attribute
			if(_examples.get(count).attributes.get(attr_idx).equals(attr_val)) { 
				newExamples.add(_examples.get(count));
			}
		}
		
		return newExamples;
		
	}
	
	
	private String[] eliminateQuestion(String[] questions, int indexOfBQ) {
		
		String[] newQuestions = new String[questions.length-1];
		if(questions[questions.length-1].equals(questions[indexOfBQ])) {
			for(int z = 0; z < questions.length-1; z++) { newQuestions[z] = questions[z]; }
		} else {
		int newIndex = 0;
			for(int h = 0; h < questions.length; h++) {
				newQuestions[newIndex] = questions[h];
				if(!questions[h].equals(questions[indexOfBQ])) { newIndex++; }
			}
		}
		return newQuestions;
	}
	
}
