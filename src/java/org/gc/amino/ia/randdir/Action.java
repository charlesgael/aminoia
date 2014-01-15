package org.gc.amino.ia.randdir;

import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;

import com.sun.org.apache.xpath.internal.operations.Minus;

// TODO: Auto-generated Javadoc
/**
 * The Class Action.
 */
public class Action {
	
	/** The Constant NB_DIVISIONS. */
	public static final int NB_DIVISIONS = 64;
	
	/** The Constant MIN_SPEED. */
	public static final double MIN_SPEED = 2;
	
	private int val;
	
	/**
	 * Instantiates a new action.
	 * 
	 * @param angle
	 *            the angle to aim for
	 */
	public Action(int angle) {
		this.val = angle;
	}
	
	/**
	 * Returns the direction represented by that action.
	 * 
	 * @return direction represented by the action
	 */
	public PointD go() {
		
		double angle = Math.toRadians(val);
		
		return new PointD(Math.cos(angle), -Math.sin(angle));
	}
		
	/**
	 * Computes where the mote must shoot in order to go where it wants.
	 * 
	 * @param me
	 *            the mote the IA is playing
	 * @return where the mote must shoot
	 */
	public PointD point(Mote me) {
		PointD pos = me.getPosition();
		PointD speed = me.getSpeed();
		double sp = Math.sqrt(Math.pow(speed.x, 2) + Math.pow(speed.y, 2));
		PointD will = go();
		
		if (will == null) return null;
		
		double x = 0, y = 0;
		
		x = speed.x - will.x;
		y = speed.y - will.y;
		
		if (sp > MIN_SPEED) {
			return null;
		}
		
		return new PointD(pos.x + x*10, pos.y + y*10);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Choosen angle: "+val+"°";
	}

	/**
	 * Returns a point that permits the mote to continue in his direction.
	 * 
	 * @param me
	 *            the mote the IA is playing
	 * @return the point asked
	 */
	public PointD pursue(Mote me) {
		PointD pos = me.getPosition();
		PointD speed = me.getSpeed();
		double sp = Math.sqrt(Math.pow(speed.x, 2) + Math.pow(speed.y, 2));
		
		double x = 0, y = 0;
		
		x = -speed.x;
		y = -speed.y;
		
		if (sp > MIN_SPEED) {
			return null;
		}
		
		return new PointD(pos.x + x*10, pos.y + y*10);
	}

}
