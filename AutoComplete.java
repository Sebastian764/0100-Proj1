/**
 * An implementation of the AutoCompleteInterface using a DLB Trie.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

 public class AutoComplete implements AutoCompleteInterface {

  private DLBNode root; //root of the DLB Trie
  private StringBuilder currentPrefix; //running prefix
  private DLBNode currentNode; //current DLBNode
  // private Set<String> dictionary = new HashSet<String>(); 
  //TODO: Add more instance variables as needed  
  
 
  @Override
  public boolean add(String word) {
    if (word == null || word.isEmpty()) throw new IllegalArgumentException("Word cannot be null or empty");

    if (root == null) {
      root = new DLBNode(word.charAt(0));
    }

    DLBNode curr = root;
    DLBNode prev = null;
    boolean isNewWord = false;

    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      DLBNode temp = null;

      // Traverse existing nodes
      while (curr != null && curr.data != c) {
        prev = curr;
        curr = curr.nextSibling;
      }

      // If current character node doesn't exist, create new
      if (curr == null) {
        temp = new DLBNode(c);
        isNewWord = true; 

        if (prev != null) { // Connect as sibling if there's a previous node in the level
          prev.nextSibling = temp;
          temp.previousSibling = prev;
        } else { // Or as a child if it's the first node in this level
          if (i == 0) root = temp; // Update root if it's the first character
          else prev.child = temp; // Set as child of the previous level node
        }
        curr = temp; 
      }

      // Update size for all nodes in the path, if new word is being added
      if (isNewWord) {
        DLBNode pathNode = curr;
        while (pathNode != null) {
          pathNode.size++;
          pathNode = pathNode.parent;
        }
      }

      // Set parent-child relationship
      if (i < word.length() - 1) {
        if (curr.child == null) {
          curr.child = new DLBNode(word.charAt(i + 1));
          curr.child.parent = curr; // Set parent
        }
        curr = curr.child; // Move down the trie
      }
    }

    if (!curr.isWord) { // Mark the end of the word if not already marked
      curr.isWord = true;
      return true; 
    }

    return false; 
  }

  private boolean traverseSibiling(char c, DLBNode startNode) {
    DLBNode curr = startNode;
    while (curr != null) {
      if (curr.data == c) {
        currentNode = curr;
        return true;
      }
      curr = curr.nextSibling;
    }
    currentNode = null; // Lose sync since character not found
    return false;
  }

  @Override
  public boolean advance(char c) {

    // update current prefix if possible
    if (currentNode == null) {
      currentPrefix = new StringBuilder();
      if (root != null) {
        return traverseSibiling(c, root);
      } else {
        return false; // No root means the trie is empty
      }
    }
    currentPrefix.append(c);
    DLBNode curr = currentNode;
    curr = curr.child;
    while (curr != null) {
      if (curr.data == c) {
        currentNode = curr;
        return true;
      }
      curr = curr.nextSibling;
    }
    return false;
  }

  @Override
  public void retreat() {
    if (currentPrefix.length() == 0) throw new IllegalArgumentException("Word cannot be null or empty");

    if (currentNode.parent != null) {
      currentNode = currentNode.parent;
      currentPrefix = currentPrefix.deleteCharAt(currentPrefix.length()-1);
    } else { // currNode == root
      reset();
    }

  }

  @Override
  public void reset() {
    currentNode = null;
    currentPrefix = new StringBuilder("");
  }

  @Override
  public boolean isWord() {
    return currentNode.isWord && currentPrefix.charAt(currentPrefix.length()-1) == currentNode.data;
  }

  @Override
  public void add() {
    currentNode.isWord = true;
    DLBNode curr = currentNode;
    // Set parent-child relationship
    for (int i = 0; i < currentPrefix.length() - 1; i++) {
      curr.size++;
      curr = curr.parent; // Move down the trie
    }
  }

  @Override
  public int getNumberOfPredictions() {
    return currentNode.size;
  }

  ArrayList<String> searchPrefix(DLBNode curr) {
    ArrayList<DLBNode> search = new ArrayList<DLBNode>();
    ArrayList<DLBNode> wordTracker = new ArrayList<DLBNode>();
    search.add(curr);
    while (!search.isEmpty()) {
      DLBNode node = search.remove(search.size() - 1);
      if (node.nextSibling != null) search.add(node.nextSibling);
      if (node.child != null) search.add(node.child);
      if (node.isWord) wordTracker.add(node);
    }

    ArrayList<String> result = new ArrayList<String>();
    while (!wordTracker.isEmpty()) {
      DLBNode node = wordTracker.remove(wordTracker.size() - 1);
      StringBuilder str = new StringBuilder();
      while (node != null) {
        str.append(node.data);
        node = node.parent;
      }
      result.add(currentPrefix + str.toString());
    }

    if (result.isEmpty()) {
      return null;
    }

    Collections.sort(result);
    return result;
  }

  @Override
  public String retrievePrediction() {
    ArrayList<String> predictions = searchPrefix(currentNode);
    if (predictions == null) return null;
    return predictions.get(0);
  }

  @Override
  public ArrayList<String> retrievePredictions() {
    return searchPrefix(currentNode);
  }  

  @Override
  public boolean delete(String word) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'delete'");
  }  

   //The DLBNode class
   private class DLBNode{
    private char data; //letter inside the node
    private int size;  //number of words in the subtrie rooted at node
    private boolean isWord; //true if the node is at the end of a word
    private DLBNode nextSibling; //doubly-linked list of siblings
    private DLBNode previousSibling;
    private DLBNode child; // child reference
    private DLBNode parent; //parent reference

    private DLBNode(char data){ //constructor
      this.data = data;
      size = 0;
      isWord = false;
    }
  }

  /* ==============================
   * Helper methods for debugging
   * ==============================
   */

  //Prints the nodes in a DLB Trie for debugging. The letter inside each node is followed by an asterisk if
  //the node's isWord flag is set. The size of each node is printed between parentheses.
  //Siblings are printed with the same indentation, whereas child nodes are printed with a deeper
  //indentation than their parents.
  public void printTrie(){
    System.out.println("==================== START: DLB Trie ====================");
    printTrie(root, 0);
    System.out.println("==================== END: DLB Trie ====================");
  }

  //a helper method for printTrie
  private void printTrie(DLBNode node, int depth){
    if(node != null){
      for(int i=0; i<depth; i++){
        System.out.print(" ");
      }
      System.out.print(node.data);
      if(node.isWord){
        System.out.print(" *");
      }
      System.out.println(" (" + node.size + ")");
      printTrie(node.child, depth+1);
      printTrie(node.nextSibling, depth);
    }
  }
}
