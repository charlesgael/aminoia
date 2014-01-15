package org.gc.amino.ia.mc;

import java.util.Random;
import java.util.Calendar;

/**
 * The Class SearchNode.
 */
public class SearchNode extends Thread {
	public static final int NB_THREADS = 2;
	public static final int NB_RUNS = 16;
	public static final int NB_COMPUTING = 1000;
	public static final int NB_TURNS = 100;  // correct = 100
	public static final int NB_RUNS_MIN = NB_COMPUTING * NB_RUNS * NB_THREADS;
	public static final long TIME_MIN = 1000;	
	public static final double EXPLORE_FACTOR = 0.3;
	public static final double MAX_SCORE = 999;
	public static final int MOVE_IN_PROGRESS = 50;
	
	private long timeStart;	
	private Board board;
	private Action action;
	private SearchNode parent;	
	private double score;
	private int tries;  
	private SearchNode[] children;
	
	/**
	 * Build a search node.
	 * 
	 * @param parent
	 *            search node which is the parent of the current search node
	 * @param b
	 * 			  board of the game
	 * @param a
	 *            associated action to the node (one among the nine directions available) 
	 */
	public SearchNode(SearchNode parent, Board b, Action a) {
		board = b;
		action = a;
		this.parent = parent;
	}
	
	/**
	 * Return the associated action to the node.
	 * 
	 * @return the associated action to the node (one among the nine directions available) 
	 */
	public Action getAction() {
		return action;
	}
	
	/**
	 * Define the behaviour of the SearchNode thread.
	 */
	public void run() {
		timeStart = Calendar.getInstance().getTime().getTime();
		startComputing();
	}
	
	/**
	 * Restart the computing for a node (go up in the search tree).
	 */
	public void restartComputing() {
		SearchNode n = this;
		while (n.parent != null)
			n = n.parent;
		
		if (n.tries < NB_RUNS_MIN)
			n.startComputing();
	}

	/**
	 * Launch the research of a solution.
	 */
	public void startComputing() {
		
		if (parent == null) {
			while(tries < NB_RUNS_MIN) {
				board.reinit();
				
				if (children == null)
					leaf();
				else 
					node();
			}
		} else {
			if (children == null)
				leaf();
			else 
				node();
		}		
	}

	private void leaf() {
		children = new SearchNode[Action.NB_DIRECTIONS];
		
		for (int i=0;i<children.length;++i) {
			children[i] = new SearchNode(this, board, Action.getAction(i));
		}
		
		simulate(children[0]);
	}

	private void node() {
		// Simulation on the first child at the beginning
		SearchNode bestChild = children[0];
		double bestScore = bestChild.score(tries);
		
		for (int i=1;i<children.length;++i) {
			double candidat = children[i].score(tries);
			if (bestScore < candidat) {
				bestChild = children[i];
				bestScore = candidat;
			}
		}
		
		// Compute on the bestChild
		bestChild.startComputing();
	}

	private double score(int n) {
		if (tries > 0)
			return score / ((double)tries) + EXPLORE_FACTOR * Math.sqrt(Math.log(n)/tries);
		else 
			return MAX_SCORE;
	}

	/**
	 * Return if the result is available.
	 * 
	 * @return a boolean if a result is available 
	 */
	public boolean isResultAvailable() {
		if (tries >= NB_RUNS_MIN) 
			return (timeStart + TIME_MIN < Calendar.getInstance().getTime().getTime());
		return false;
	}

	/**
	 * Return the node which is the best child.
	 * 
	 * @return a search node which is the best child
	 */
	public SearchNode getBestChild() {
		SearchNode bestChild = children[0];
		double bestScore = bestChild.score(tries);			
		
		// DEBUG
		for (int i=0;i<children.length;++i) {
			System.out.println(children[i].score(tries));
		}
		
		for (int i=1;i<children.length;++i) {
			double candidat = children[i].score(tries);
			if (bestScore < candidat) {
				bestChild = children[i];
				bestScore = candidat;
			}
		}
		
		return bestChild;
	}

	private void simulate(SearchNode bestChild) {		
		Thread[] threads = new Thread[NB_THREADS];
		
		Random r = new Random();
		for(int t = 0;t<NB_THREADS;++t) {
			threads[t] = new Simulation(board, this, r);
			threads[t].start();
		}

		for (int t = 0;t<NB_THREADS;++t) {
			try {
				threads[t].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add obtained evaluation to the score of the current and parent node.
	 * 
	 * @param eval
	 *            evaluation of the node after simulation on it
	 */
	public void addScore(double eval) {
		//System.out.println("ADD SCORE -> eval =  "+eval);
		score += eval;
		//System.out.println("ADD SCORE -> score =  "+score);
		++tries;
		
		if (parent != null)
			parent.addScore(eval);
	}
}
