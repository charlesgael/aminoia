package org.gc.amino.ia.randdir;

// TODO: Auto-generated Javadoc
/**
 * The Class SearchNode.
 */
public class SearchNode {

	
	private Board board;
	private Action action;
	private SearchNode parent;

	/**
	 * Instantiates a new search node.
	 * 
	 * @param parent
	 *            the parent node in the tree
	 * @param b
	 *            the board corresponding to the state of this node
	 * @param a
	 *            the action done by this mote
	 */
	public SearchNode(SearchNode parent, Board b, Action a) {
		this.board = b;
		this.action = a;
		this.parent = parent;
	}
}
