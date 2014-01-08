package org.gc.amino.ia.mc;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
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
	
	private Action lastAction;
	private int nbActions;

    public static void main( String[] argv ) throws IOException {
        IaLauncher.launch( argv, new MontecarloIa() );
    }

	@Override
	public void init(PointD size) {
		Game.init(new TerrainMap(size));
        acceptFrame = true;
        //actions = new LinkedList<>();
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
			
			Iterator<Mote> it = otherMotes.iterator();
			
			while(it.hasNext()){
				Mote m = it.next();
				if (m.getRadius() < you.getRadius()*0.05)
					it.remove();
			}
			
			Board b = new Board(you, otherMotes);
			mc = new SearchNode(null, b, null);
			mc.start();
		}
		else if (mc.isResultAvailable()) {
			Action a = mc.getBestChild().getAction();
			System.out.println("Result available "+a);
			
			mc = null;
			
			if (!isBigEnough(you, otherMotes)) {
				System.out.println("Not big enough : PLAY");
				if (lastAction == null || !lastAction.isOpposite(a))
				{
					System.out.println("Not opposite direction");
					lastAction = a;
					return lastAction.fastPoint(you);
				}
				else {
					lastAction = null;
				}
			}
		}
		else if (speed < 0.2 && !goodAngle(you.getSpeed(), you)) {
			return lastAction.fastPoint(you);
		}
		
		return null;
	}

	private boolean goodAngle(PointD speed, Mote you) {
		if (lastAction == null) return true;
		PointD will = lastAction.fastPoint(you);
		if (will == null) return true;
		
		double angle = 0;
		
		if (speed.x > 0) {
			if (speed.y >= 0) {
				angle = Math.atan(speed.y / speed.x);
			}
			else {
				angle = Math.atan(speed.y / speed.x) + 2* Math.PI;
			}
		}
		else if (speed.x < 0) {
			angle = Math.atan(speed.y / speed.x) + Math.PI;
		}
		else {
			if (speed.y >= 0) {
				angle = Math.PI /2;
			}
			else {
				angle = 3*Math.PI /2;
			}
		}
		
		double angle2 = 0;
		
		if (will.x > 0) {
			if (will.y >= 0) {
				angle2 = Math.atan(will.y / will.x);
			}
			else {
				angle2 = Math.atan(will.y / will.x) + 2* Math.PI;
			}
		}
		else if (will.x < 0) {
			angle2 = Math.atan(will.y / will.x) + Math.PI;
		}
		else {
			if (will.y >= 0) {
				angle2 = Math.PI /2;
			}
			else {
				angle2 = 3*Math.PI /2;
			}
		}
		
		System.out.println("Angle1 : "+angle+"\nAngle2 : "+angle2+"\nGood : "+(Math.abs(angle2 - angle) < Math.PI /2));
		
		return Math.abs(angle2 - angle) < Math.PI /2;
	}

	private boolean isBigEnough(Mote you, List<Mote> otherMotes) {
		double maxRadius = 0;
		
		for (Mote m : otherMotes) {
			maxRadius = Math.max(maxRadius, m.getRadius());
		}
		
		return you.getRadius()*0.9 > maxRadius;
	}
}
