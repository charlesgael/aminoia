package org.gc.amino.ia.mc;

import java.util.Random;

/**
 * The Class Simulation.
 */
public class Simulation extends Thread {
	private Board board;
	private SearchNode launcher;
	private Random random;
	int nbTurns;
	
	/**
	 * Instantiates a new simulation.
	 * 
	 * @param board
	 *            the board to use for the simulation
	 * @param launcher
	 *            the launcher that started that simulation
	 * @param random
	 *            the random generator
	 */
	public Simulation(Board board, SearchNode launcher, Random random) {
		super("SimulationThread");
		this.board = board.save();
		this.launcher = launcher;
		this.random = random;
		nbTurns=SearchNode.MOVE_IN_PROGRESS;
	}
	
	public void run() {
		
		Action act = null;		
		
		for (int r = 0;r<SearchNode.NB_RUNS;++r) {
			board.reinit();
			
			for (int a = 0;a<SearchNode.NB_TURNS && !board.isFinished();++a) {				
				if(timeMove() && act != null) {
					board.move(act.fastPoint(board.getMe()));	
					board.nextTurn();
					nbTurns++;
				} else {
					act = Action.getAction(random.nextInt(Action.NB_DIRECTIONS));	
					board.move(act.fastPoint(board.getMe()));				
					board.nextTurn();
					nbTurns = 0;
				}
			}
			
			double eval = board.eval();
			launcher.addScore(eval);
		}
	}
	
	private boolean timeMove() {
		return nbTurns < SearchNode.MOVE_IN_PROGRESS;
	}
}
