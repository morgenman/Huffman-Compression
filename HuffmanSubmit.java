
// Dylan Morgen
// Project 3
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

// Import any package as required

public class HuffmanSubmit implements Huffman {
	static HashMap<Character, String>	dictionary	= new HashMap<Character, String>();	// Used in encoding (both are cleared before decoding occurs)
	static HashMap<String, Character>	dictionary2	= new HashMap<String, Character>();	// used in decoding

	// Feel free to add more methods and variables as required. 

	public void encode(String inputFile, String outputFile, String freqFile) {
		BinaryIn in = new BinaryIn(inputFile);
		BinaryOut out = new BinaryOut(outputFile);
		char i;//just a temp storage thingy
		HashMap<Character, Integer> db = new HashMap<Character, Integer>(); // Holds how many time a character appears
		while (!in.isEmpty()) {
			//puts them in the database
			i = in.readChar();
			if (db.containsKey(i)) db.put(i, db.get(i) + 1);
			else db.put(i, 1);
		}
		db = sort(db); //sort it by frequency for increased spead in later processes
		ArrayList<BTNode> tree = new ArrayList<BTNode>(); // Holds all the nodes
		for (HashMap.Entry<Character, Integer> i1 : db.entrySet()) {
			tree.add(new BTNode(i1.getKey(), i1.getValue(), true)); //add all the nodes to the tree
		}

		while (tree.size() > 1) {
			//find the two smallest nodes and combine into one node, update count
			BTNode min = null, min2 = null;
			for (BTNode i1 : tree) {
				if (min == null || i1.getCount() < min.getCount()) { //if this is the first time running or the current node is smaller than the min node
					min = i1; //the smallest is the current
				}
				else {
					if (min2 == null || i1.getCount() < min2.getCount()) { //same thing for the second smallest
						min2 = i1;
					}
				}
			}
			tree.remove(min);
			tree.remove(min2);
			tree.add(new BTNode(min, min2, min.getCount() + min2.getCount())); //update count, combine into one node
		}

		level_order(tree.get(0)); //should only be one node in the tree, generate the dictionary hashmap with the binary encoding
		in = new BinaryIn(inputFile); //re-add input file
		while (!in.isEmpty()) {
			i = in.readChar(); //go through each character
			for (char i4 : dictionary.get(i).toCharArray()) { //get the encoding from the dictionary
				if (i4 == '0') out.write(false); //if its 0 write a 0
				else out.write(true);//if its 1 write a 1
			}

		}

		BinaryOut freq = new BinaryOut(freqFile);// generate the frequency file 
		for (HashMap.Entry<Character, Integer> i1 : db.entrySet()) {
			freq.write(Integer.toBinaryString(i1.getKey()) + ":" + i1.getValue() + "\n"); //add to frequency file
			freq.flush();
		}

		freq.close();//close everything
		out.close();

	}

	public void level_order(BTNode root) {
		for (int i = 0; i < height(root); i++) { //run for every 'level'
			level(root, i + 1); //
		}
	}

	static void level(BTNode root, int level) {
		if (root != null) {
			if (level == 1 && root.isLeaf()) { //if its a leaf, go through all of its parents and grandparents etc adding 1's and 0's based on if its left or right
				BTNode now = root;
				String temp = "";
				while (now.getParent() != null) {
					temp = now.getLr() + temp;
					now = now.getParent();
				}

				dictionary2.put(temp, (char) root.getData()); // used in decoding
				dictionary.put((char) root.getData(), temp); //used in encoding
			}

			else if (level > 1) {
				level(root.getLeft(), level - 1); //go through the left hand side of the node recursively
				level(root.getRight(), level - 1); //go through the righthand side recursively
			}

		}
	}

	int height(BTNode root) {
		//get the height of the tree
		if (root == null) return 0;
		else {
			int lh = height(root.getLeft());
			int rh = height(root.getRight());
			if (lh > rh) return (lh + 1);
			else return (rh + 1);
		}
	}

	public HashMap<Character, Integer> sort(HashMap<Character, Integer> passedMap) {
		//sort the hashmap from smalle
		List<Character> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues); //sort values and keys
		Collections.sort(mapKeys);
		LinkedHashMap<Character, Integer> sortedMap = new LinkedHashMap<>();//output
		Iterator<Integer> valueIt = mapValues.iterator(); //iterator
		while (valueIt.hasNext()) { //iterate through all map values
			Integer val = valueIt.next();
			Iterator<Character> keyIt = mapKeys.iterator(); //iterate through the keys
			while (keyIt.hasNext()) { //for every key
				Character key = keyIt.next();
				Integer comp1 = passedMap.get(key); //the value
				Integer comp2 = val;
				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;

	}

	public void decode(String inputFile, String outputFile, String freqFile) {
		dictionary.clear();
		dictionary2.clear();//make sure these are clear
		BinaryOut out = new BinaryOut(outputFile);
		BinaryIn freq = new BinaryIn(freqFile);
		HashMap<Character, Integer> db = new HashMap<Character, Integer>();
		String temp = "", key = "", value = "";
		char t;
		while (!freq.isEmpty()) { //map the frequency file to a db
			t = freq.readChar();
			if (t == ':') {
				key = temp;
				temp = "";
			}
			else if (t == '\n') {
				value = temp;
				temp = "";
				db.put((char) Integer.parseInt(key, 2), Integer.parseInt(value)); // add character from binary representation
			}
			else temp = temp + t;
		}
		db = sort(db); //sort it
		ArrayList<BTNode> tree = new ArrayList<BTNode>();
		tree.ensureCapacity(db.size());
		for (HashMap.Entry<Character, Integer> i1 : db.entrySet()) {
			tree.add(new BTNode(i1.getKey(), i1.getValue(), true));//add all nodes to the tree
		}

		while (tree.size() > 1) {
			//same code as above
			BTNode min = null, min2 = null;
			for (BTNode i1 : tree) {
				if (min == null || i1.getCount() < min.getCount()) {
					min = i1;
				}
				else {
					if (min2 == null || i1.getCount() < min2.getCount()) {
						min2 = i1;
					}
				}
			}
			tree.remove(min);
			tree.remove(min2);
			tree.add(new BTNode(min, min2, min.getCount() + min2.getCount()));
		}

		level_order(tree.get(0));
		temp = "";
		out.flush();
		BinaryIn in = new BinaryIn(inputFile);

		while (!in.isEmpty()) {
			//take the in, find the entry in the dictionary, output it
			if (in.readBoolean() == false) temp += 0;
			else temp += 1;
			if (dictionary2.containsKey(temp)) {
				out.flush();
				out.write(dictionary2.get(temp));
				out.flush();
				temp = "";
			}

		}
		out.close();

		//if (temp.length() > 1) out.write(dictionary2.get(temp));
	}

	public static void main(String[] args) {
		Huffman huffman = new HuffmanSubmit();
		huffman.encode("alice30.txt", "alice30.enc", "freq.txt");
		huffman.encode("ur.jpg", "ur.enc", "freqj.txt");

		huffman.decode("alice30.enc", "alice_decoded.txt", "freq.txt");
		huffman.decode("ur.enc", "ur_decoded.jpg", "freqj.txt");

		// After decoding, both ur.jpg and ur_dec.jpg should be the same. 
		// On linux and mac, you can use `diff' command to check if they are the same. 

	}

	public class BTNode {
		private int lr; //true = 1 (right) false = 0 (left)// this holds the side of the branch its on

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @param count
		 *            the count to set
		 */
		public void setCount(int count) {
			this.count = count;
		}

		private char	data;
		private int		count;
		private boolean	isLeaf;

		/**
		 * @return the isLeaf
		 */
		public boolean isLeaf() {
			return isLeaf;
		}

		/**
		 * @param isLeaf
		 *            the isLeaf to set
		 */
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
		}

		private BTNode	left;
		private BTNode	right;
		private BTNode	parent	= null;

		/**
		 * @return the lr
		 */
		public int getLr() {
			return lr;
		}

		/**
		 * @param lr
		 *            the lr to set
		 */
		public void setLr(int lr) {
			this.lr = lr;
		}

		/**
		 * @param data
		 *            the data to set
		 */
		public void setData(char data) {
			this.data = data;
		}

		/**
		 * @param left
		 *            the left to set
		 */
		public void setLeft(BTNode left) {
			this.left = left;
		}

		/**
		 * @param right
		 *            the right to set
		 */
		public void setRight(BTNode right) {
			this.right = right;
		}

		// Add constructor and/or other methods if required
		public BTNode(Character data, Integer count) {
			this.count = count;
			this.data = data;
			this.left = null;
			this.right = null;
		}

		public BTNode(BTNode left, BTNode right, int count) {
			left.lr = 0;
			right.lr = 1;
			left.parent = this;
			right.parent = this;
			this.count = count;
			this.left = left;
			this.right = right;
		}

		public BTNode(char data, BTNode right, int count) {
			right.parent = this;
			this.count = count;
			this.data = data;
			this.left = null;
			this.right = right;
		}

		/**
		 * @return the parent
		 */
		public BTNode getParent() {
			return parent;
		}

		/**
		 * @param parent
		 *            the parent to set
		 */
		public void setParent(BTNode parent) {
			this.parent = parent;
		}

		/**
		 * @param key
		 * @param value
		 * @param b
		 */
		public BTNode(Character data, Integer count, boolean b) {
			this.count = count;
			this.data = data;
			this.left = null;
			this.right = null;
			this.isLeaf = b;
		}

		/**
		 * @return the data
		 */
		public char getData() {
			return data;
		}

		/**
		 * @return the left
		 */
		public BTNode getLeft() {
			return left;
		}

		/**
		 * @return the right
		 */
		public BTNode getRight() {
			return right;
		}
	}

}
