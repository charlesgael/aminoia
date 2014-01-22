package org.gc.amino.ia.mc;

import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;

/**
 * The Enumeration Action.
 */
public enum Action {
	NOTHING(0),
	NORTH(1),
	NORTH_EAST(2),
	EAST(3),
	SOUTH_EAST(4),
	SOUTH(5),
	SOUTH_WEST(6),
	WEST(7),
	NORTH_WEST(8);
	
	/**
	 * Gets the action corresponding to the value.
	 * 
	 * @param value
	 *            the value
	 * @return the action
	 */
	public static Action getAction(int value) {
		switch(value) {
		case 0:
			return NOTHING;
		case 1:
			return NORTH;
		case 2:
			return NORTH_EAST;
		case 3:
			return EAST;
		case 4:
			return SOUTH_EAST;
		case 5:
			return SOUTH;
		case 6:
			return SOUTH_WEST;
		case 7:
			return WEST;
		case 8:
			return NORTH_WEST;
		}
		return null;
	}
	
	/** The number of directions available. */
	public static final int NB_DIRECTIONS = 9;
	private int val;
	
	private Action(int val) {
		this.val = val;
	}
	
	/**
	 * Returns the direction represented by that action.
	 * 
	 * @return direction represented by the action
	 */
	public PointD go() {
		switch(this) {
			case NORTH:
				return new PointD(0, 100);
			case NORTH_EAST:
				return new PointD(-50, 50);
			case EAST:
				return new PointD(-100, 0);
			case SOUTH_EAST:
				return new PointD(-50, -50);
			case SOUTH:
				return new PointD(0, -100);
			case SOUTH_WEST:
				return new PointD(50, -50);
			case WEST:
				return new PointD(100, 0);
			case NORTH_WEST:
				return new PointD(50, 50);
			case NOTHING:
				return null;
		}
		return null;
	}

	/**
	 * Computes where the mote must shoot in order to go where it wants.
	 * 
	 * @param me
	 *            the mote the IA is playing
	 * @return where the mote must shoot
	 */
	public PointD fastPoint(Mote me) {
		PointD pos = me.getPosition();
		PointD add = go();
		
		if (add != null) {
			pos.set(pos.x + add.x, pos.y + add.y);
			return pos;
		}
		return null;
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
		PointD will = go();
		
		if (will == null) return null;
		
		double x = 0, y = 0;
		
		x = speed.x - will.x;
		y = speed.y - will.y;
		
		if (Math.abs(x) < 0.05) {
			return null;
		}
		
		return new PointD(pos.x + x*10, pos.y + y*10);
	}

	/**
	 * Checks if the direction in argument is opposite to that direction.
	 * 
	 * @param action
	 *            the action
	 * @return true, if is opposite
	 */
	public boolean isOpposite(Action action) {
		if (this == NOTHING)
			return false;
		
		return (((val-1)+4)%8)+1 == action.val
				|| (((val-1)+3)%8)+1 == action.val
				|| (((val-1)+5)%8)+1 == action.val;
	}
	
	/**
	 * Display the label of the current action
	 * 
	 * @return label of the action (String)
	 */
	public String toString() {
		switch(this) {
			case NOTHING:
				return "NOTHING";
			case NORTH:
				return "NORTH";
			case NORTH_EAST:
				return "NORTH_EAST";
			case EAST:
				return "EAST";
			case SOUTH_EAST:
				return "SOUTH_EAST";
			case SOUTH:
				return "SOUTH";
			case SOUTH_WEST:
				return "SOUTH_WEST";
			case WEST:
				return "WEST";
			case NORTH_WEST:
				return "NORTH_WEST";
		}
		return null;
	}
}
