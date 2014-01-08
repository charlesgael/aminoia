package org.gc.amino.ia.mc;

import java.util.List;
import java.util.Random;
import java.util.Calendar;

import org.gc.amino.engine.mote.Mote;

public class SearchNode extends Thread {
	public static final int NB_THREADS = 2;
	public static final int NB_RUNS = 4;
	public static final int NB_COMPUTING = 150;
	public static final int NB_TURNS = 500;
	public static final int NB_RUNS_MIN = NB_COMPUTING * NB_RUNS * NB_THREADS;
	public static final long TIME_MIN = 1000;
	
	public static final double EXPLORE_FACTOR = 0.3;
	
	private long timeStart;
	
	private Board board;
	private Action action;
	private SearchNode parent;
	
	private double score;
	private int tries;
	
	private SearchNode[] children;
	
	public SearchNode(SearchNode parent, Board b, Action a) {
		board = b;
		action = a;
		this.parent = parent;
	}
	
	public void newRoot() {
		parent = null;
	}
	
	public Action getAction() {
		return action;
	}
	
	public void run() {
		timeStart = Calendar.getInstance().getTime().getTime();
		startComputing();
	}
	
	public void restartComputing() {
		SearchNode n = this;
		while (n.parent != null)
			n = n.parent;
		
		if (n.tries < NB_COMPUTING * NB_RUNS * NB_THREADS)
			n.startComputing();
	}

	/**
	 * Lance la recherche de solution.
	 */
	public void startComputing() {
		if (parent == null) board.reinit();
		
		if (children == null){
			leaf();
		}
		else {
			node();
		}
	}

	private void leaf() {
		children = new SearchNode[Action.NB_DIRECTIONS];
		
		for (int i=0;i<children.length;++i) {
			children[i] = new SearchNode(this, board, Action.getAction(i));
		}
		
		simulate(children[0]);
		
		restartComputing();
	}

	private void node() {
		SearchNode bestChild = children[0];
		double bestScore = bestChild.score(tries);
		
		for (int i=1;i<children.length;++i) {
			double candidat = children[i].score(tries);
			if (bestScore < candidat) {
				bestChild = children[i];
				bestScore = candidat;
			}
		}
		
		bestChild.startComputing();
	}

	private double score(int n) {
		if (tries > 0) {
			return score / ((double)tries) + EXPLORE_FACTOR * Math.sqrt(Math.log(n)/tries);
		}
		else {
			return 999;
		}
	}

	public boolean isResultAvailable() {
		//if (tries % 20 == 0) System.out.println("test : "+tries+"/"+NB_RUNS_MIN);
		if (tries >= NB_RUNS_MIN) {
			return (timeStart + TIME_MIN < Calendar.getInstance().getTime().getTime());
		}
		return false;
	}

	public SearchNode getBestChild() {
		SearchNode bestChild = children[0];
		double bestScore = bestChild.score(tries);
		//System.out.println(bestChild.action+" : "+bestScore);
		
		for (int i=1;i<children.length;++i) {
			double candidat = children[i].score(tries);
			//System.out.println(children[i].action+" : "+candidat);
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
		for (int t = 0;t<NB_THREADS;++t) {
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

	public void addScore(double eval) {
		score += eval;
		++tries;
		
		if (parent != null)
			parent.addScore(eval);
	}
}
