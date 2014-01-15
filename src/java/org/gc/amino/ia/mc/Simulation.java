package org.gc.amino.ia.mc;

import java.util.Random;

public class Simulation extends Thread {
	private Board board;
	private SearchNode launcher;
	private Random random;
	int nbTurns;
	
	public Simulation(Board b, SearchNode l, Random r) {
		super("SimulationThread");
		board = b.save();
		launcher = l;
		random = r;
		nbTurns=SearchNode.MOVE_IN_PROGRESS;
	}
	
	public void run() {
		
		Action act = null;		
		
		for (int r = 0;r<SearchNode.NB_RUNS;++r) {
			board.reinit();
			
			for (int a = 0;a<SearchNode.NB_TURNS && !board.isFinished();++a) {
				
				/*if(timeMove() && act != null) {
					System.out.println("ATTENTE");
					board.move(act.fastPoint(board.getMe()));	
					board.nextTurn();
					nbTurns++;
				} else {*/	
					//System.out.println("NEW CALCUL");
					act = Action.getAction(random.nextInt(Action.NB_DIRECTIONS));				
					board.move(act.fastPoint(board.getMe()));				
					board.nextTurn();
					/*nbTurns = 0;
				}*/
			}
			
			double eval = board.eval();
			launcher.addScore(eval);
		}
	}
	
	private boolean timeMove() {
		return nbTurns < SearchNode.MOVE_IN_PROGRESS;
	}
}
