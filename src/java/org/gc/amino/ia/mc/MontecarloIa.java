package org.gc.amino.ia.mc;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.gc.amino.engine.Game;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.engine.terrainmap.TerrainMap;
import org.gc.amino.ia.httpserver.IaDeliveryInterface;
import org.gc.amino.ia.httpserver.IaLauncher;

public class MontecarloIa implements IaDeliveryInterface {
	public static final long INTERVAL = 1000;

	private Mote me;
	
	private boolean acceptFrame;
	private long nextFrame;
	private SearchNode mc;
	
	private LinkedList<Action> actions;

    public static void main( String[] argv ) throws IOException {
        IaLauncher.launch( argv, new MontecarloIa() );
    }

	@Override
	public void init(PointD size) {
		Game.init(new TerrainMap(size));
        acceptFrame = true;
        actions = new LinkedList<>();
	}

	@Override
	public PointD frame(Mote you, List<Mote> otherMotes) {
		/*if (acceptFrame) {
			PointD action = null;
			if (mc != null && mc.isResultAvailable()) {
				System.out.println("Result available "+mc.getBestMove());
				action = mc.getBestMove().point(you);
			}
			else {
				System.out.println("No result");
			}

			System.out.println("Next computation");
			acceptFrame = false;
			nextFrame = Calendar.getInstance().getTime().getTime() + INTERVAL;
			
			Board b = new Board(you, otherMotes);
			mc = new SearchNode(null, b, null);
			mc.startComputing();
			
			return action;
		}
		else if(Calendar.getInstance().getTime().getTime() > nextFrame) {
			acceptFrame = true;
		}*/
		
		double speed = Math.sqrt( Math.pow(you.getSpeed().x, 2) + Math.pow(you.getSpeed().y, 2));
		
		if (mc == null) {
			System.out.println("Next computation");
			
			Board b = new Board(you, otherMotes);
			mc = new SearchNode(null, b, null);
			mc.start();
		}
		else if (mc.isResultAvailable()) {
			System.out.println("Result available "+mc.getBestChild().getAction());
			mc = mc.getBestChild();
			mc.newRoot();
			
			for (int i=0;i<4;++i)
				if (mc.getAction() != Action.NOTHING)
					actions.addFirst(mc.getAction());
			
			mc.start();
		}
		else if (!actions.isEmpty()) {
			return actions.pop().fastPoint(you);
		}
		/*else if (speed < 0.2 && lastMove != Action.NOTHING) {
			PointD action = lastMove.point(you);
			try {
				System.out.println("speed up mypos("+you.getPosition().x+","+you.getPosition().y+") tir("+action.x+","+action.y+")");
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
			return action;
		}*/
		
		return null;
	}
}
