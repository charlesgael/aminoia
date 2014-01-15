package org.gc.amino.ia.randdir;


/**
 * The Class SearchNode.
 * 
 * This is the core of the IA. It's a tree based search node that permits
 * to have a representation of the research.
 */
public class SearchNode extends Thread {

	/** The number of threads to launch. */
	public static final int NB_THREADS = 1;
	/** The number of turns to simulate. */
	public static final int NB_TURNS = 1000;
	
	private Board board;
	private Action action;
	private SearchNode parent;
	
	private SearchNode[] children;

	private double score;

	private boolean finished;
	private Action choosenAction;
	private double choosenScore;

	/**
	 * Instantiates a new search node.
	 * 
	 * That search node will have 1 children for each direction available in
	 * 
	 * @param parent
	 *            the parent node in the tree
	 * @param board
	 *            the board corresponding to the state of this node
	 * @param action
	 *            the action done by this mote
	 *            {@link com.gc.amino.ia.randdir.Action}
	 * @see {@link org.gc.amino.ia.randdir.Action}
	 */
	public SearchNode(SearchNode parent, Board board, Action action) {
		this.board = board.save();
		this.action = action;
		this.parent = parent;
	}
	
	private void computeDirection() {
		//System.out.println("me "+board.getMe().getPosition().x+ ", "+board.getMe().getPosition().y);
		board.move(action.point(board.getMe()));
		
		// Computes for the number of turns determined in SearchNode
		try{
			for (int run = 0;
					run < SearchNode.NB_TURNS;
					++run) {
				//System.out.println("me "+board.getMe().getPosition().x+ ", "+board.getMe().getPosition().y);
				
				board.nextTurn();
				
				board.move(action.pursue(board.getMe()));
			}
			score = board.eval();
		}catch(IllegalStateException e) {
			System.out.println("died");
			score = 0;
		}
		//System.exit(-1);
	}
	
	/**
	 * Gets the chosen action.
	 * 
	 * @return the chosen action
	 */
	public Action getChoosenAction() {
		return this.choosenAction;
	}
	
	/**
	 * Initializes the children.
	 */
	public void initChildren() {
		children = new SearchNode[Action.NB_DIVISIONS];
		
		// For each angle available through the action subdivision system
		for (int i=0, angle = 0;
				i < Action.NB_DIVISIONS;
				++i, angle += 360 / Action.NB_DIVISIONS) {
			
			Action a = new Action(angle);
			children[i] = new SearchNode(this, board, a);
		}
	}

	/**
	 * Checks if computation is finished.
	 * 
	 * @return true, if is computation finished
	 */
	public boolean isComputationFinished() {
		return this.finished;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		finished = false;
		
		if (parent == null) {
			runChildren();
		}
		else {
			computeDirection();
		}
		
		finished = true;
	}

	private void runChildren() {
		choosenScore = -100;
		
		for (int i=0;
				i<children.length;
				++i) {
			children[i].start();
			
			// Not launch more than a certain number of threads
			if ((i+1) % NB_THREADS == 0) {
				for (int c=NB_THREADS-1;
						c>=0;
						--c) {
					try {
						children[i-c].join();
					} catch (InterruptedException e) {}
					
					double score = children[i-c].score;
					Action action = children[i-c].action;
					
					if (score > choosenScore) {
						choosenScore = score;
						choosenAction = action;
					}
				}
			}
		}
		System.out.println("BEST "+choosenAction+": "+((choosenScore==0) ? "died" : choosenScore));
	}
}
