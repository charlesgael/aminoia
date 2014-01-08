package org.gc.amino.ia.mc;

import java.util.Random;

public class Simulation extends Thread {
	private Board board;
	private SearchNode launcher;
	private Random random;
	
	public Simulation(Board b, SearchNode l, Random r) {
		board = b.save();
		launcher = l;
		random = r;
	}
	
	public void run() {
		for (int r = 0;r<SearchNode.NB_RUNS;++r) {
			board.reinit();
			
			for (int a = 0;a<SearchNode.NB_TURNS && !board.isFinished();++a) {
				Action act = Action.getAction(random.nextInt(Action.NB_DIRECTIONS));
				
				board.move(act.fastPoint(board.getMe()));
				
				board.nextTurn();
			}
			
			double eval = board.eval();
			launcher.addScore(eval);
		}
	}
}
