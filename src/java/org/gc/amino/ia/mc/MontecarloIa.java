package org.gc.amino.ia.mc;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.gc.amino.engine.Game;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.engine.terrainmap.TerrainMap;
import org.gc.amino.ia.httpserver.IaDeliveryInterface;
import org.gc.amino.ia.httpserver.IaLauncher;

/**
 * The Class montecarloIa.
 */
public class MontecarloIa implements IaDeliveryInterface {
	/*private Mote me;
	private boolean acceptFrame;
	private long nextFrame;
	private int nbActions;*/
	
	private SearchNode mc;
	private Action lastAction;
	
	/**
	 * Launch the MonteCarloIa.
	 * 
	 * @param argv
	 *            parameters given to the program
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main( String[] argv ) throws IOException {
		IaLauncher.launch( argv, new MontecarloIa() );
	}

	/**
	 * Initialize the game of a given size.
	 * 
	 * @param size
	 *            size of the map
	 */
	public void init(PointD size) {
		Game.init(new TerrainMap(size));
		//acceptFrame = true;
		//actions = new LinkedList<>();
	}

	/**
	 * Launch the MonteCarloIa.
	 * 
	 * @param you
	 *            the current mote
	 * 
	 * @param otherMotes
	 *            all the others motes in the environment
	 * @return a new direction
	 */
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

		// Compute the current speed of our mote
		double speed = Math.sqrt( Math.pow(you.getSpeed().x, 2) + Math.pow(you.getSpeed().y, 2));

		if (mc == null) {
			System.out.println("--- Next computation ---");
			Iterator<Mote> it = otherMotes.iterator();

			// Filter : ignore very small closed remotes (gain in compuation)
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

	// TO DO : SIMPLIFY
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

		//System.out.println("Angle2 : "+(angle2 - angle)+"\nGood : "+(Math.abs(angle2 - angle) < Math.PI /2));

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
