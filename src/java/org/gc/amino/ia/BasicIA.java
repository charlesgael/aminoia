package org.gc.amino.ia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.ia.httpserver.IaDeliveryInterface;
import org.gc.amino.ia.httpserver.IaLauncher;
import org.gc.amino.util.Triplet;

public class BasicIA implements IaDeliveryInterface {
	
	private static final int IN_THRESHOLD = 500;
	private static final int UNDER_THRESHOLD = 100;
	private static final int RUNNING_IN_BIG = -50;
	private static final int TOO_BIG_BEHIND = -600;
	private static final int EATABLE_BIG_BEHIND = 100;
	
	private static final double GOOD_DIRECTION_MULTIPLIER = 1.4;
	private static final double BAD_DIRECTION_MULTIPLIER = 0.6;
	
	private static final double MAX_CRUISE_SPEED = 1;

	private PointD terrainSize;
	private MoteWrapper me;

	@Override
	public void init(PointD size) {
        terrainSize = size;
	}

	@Override
	public PointD frame(Mote you, List<Mote> otherMotes) {
		me = new MoteWrapper(you);
		
		Collections.sort(otherMotes, new MoteComparator(me));

		boolean[] dirCalculated = new boolean[Direction.NB_DIR];
		double[] dir = new double[Direction.NB_DIR];
		double maxProp = 0.75f;
		double minProp = 0.70f;
		
		for (Mote mote : otherMotes) {
			PointD relLoc = me.relativeLocation(mote);
			Direction quarter = me.getQuarter(mote);
			
			if (!dirCalculated[quarter.getVal()]) {
				double prop = me.getProportion(mote);
				double val = 0;
				if (prop <= maxProp && prop > minProp) {
					val += IN_THRESHOLD;
					List<Mote> thatDir = getMotesInDirection(otherMotes, quarter);
					
					for (Mote mote2 : thatDir) {
						if (mote != mote2) {
							double prop2 = me.getProportion(mote2);
							if (getArea(prop2*100) > getArea(100) + getArea(prop*100)) {
								val += TOO_BIG_BEHIND;
							}
							else {
								val += EATABLE_BIG_BEHIND;
							}
						}
					}
				}
				else if (prop > maxProp) {
					val += RUNNING_IN_BIG;
				}
				else {
					val += UNDER_THRESHOLD;
				}
				
				dir[quarter.getVal()] = val * me.getDirectionMultiplier(quarter);
				dirCalculated[quarter.getVal()] = true;
			}
		}
		
		int bestDirection = 0;
		for (int i=1;i<Direction.NB_DIR;i++) {
			if (dirCalculated[i]) {
				if (dir[i] > dir[bestDirection]){
					bestDirection = i;
				}
			}
		}
		
		System.out.println("Actual direction&speed : "+me.getDirection()+" "+me.getInercy());
		System.out.println("Best direction : "+bestDirection+":"+Direction.get(bestDirection)+" ("+dir[bestDirection]+")");
		
		int danger = -1;
		for (int i=0;i<Direction.NB_DIR;i++) {
			if (dirCalculated[i]) {
				if ((danger != -1 && dir[i] < dir[danger]) || (danger == -1 && dir[i] < 0)){
					danger = i;
				}
			}
		}
		
		if (danger != -1 && dir[bestDirection] + dir[danger] < 0) {
			if (me.getDirection() == Direction.get(danger).reverse() && me.getInercy() < MAX_CRUISE_SPEED)
				return Direction.get(danger).reverse().getPointD(me);
		}
		
		if (me.getDirection() == Direction.get(bestDirection) && me.getInercy() < MAX_CRUISE_SPEED || me.getDirection() != Direction.get(bestDirection)) {
			return Direction.get(bestDirection).getPointD(me);
		}
		
		return null;
	}
	
	public double getArea(double radius) {
		return (double) (Math.PI * (double) (Math.pow(radius, 2)));
	}
	
	public List<Mote> getMotesInDirection(List<Mote> otherMotes, Direction quarter) {
		ArrayList<Mote> ret = new ArrayList<>();
		for (Mote mote : otherMotes) {
			if (me.getQuarter(mote) == quarter)
				ret.add(mote);
		}
		return ret;
	}

    public static void main( String[] argv ) throws IOException {
        IaLauncher.launch( argv, new BasicIA() );
    }
    
    class MoteComparator implements Comparator<Mote> {
    	Mote me;
    	public MoteComparator(Mote you) {
    		me = you;
    	}
    	
		public int compare(Mote o1, Mote o2) {
			return (int) (me.getDistance(o1) - me.getDistance(o2));
		}
    }
    
    public enum Direction {
    	NORTH_WEST(0),
    	NORTH_EAST(1),
    	SOUTH_EAST(2),
    	SOUTH_WEST(3);
    	
    	public static final int NB_DIR = 4;
    	
    	private int val;
    	
    	Direction(int val) {
    		this.val = val;
    	}

		public PointD getPointD(Mote you) {
    		PointD pos = you.getPosition();
    		int x=0,y=0;
    		
    		if (val == 0){
    			x=1;y=1;
    		}
    		if (val == 1){
    			x=-1;y=1;
    		}
    		if (val == 2){
    			x=-1;y=-1;
    		}
    		if (val == 3){
    			x=1;y=-1;
    		}
    		
			return new PointD(pos.x+x, pos.y+y);
		}

		public int getVal() {
    		return val;
    	}
    	
    	public static Direction get(int val) {
    		if (val == 0)
    			return NORTH_WEST;
    		if (val == 1)
    			return NORTH_EAST;
    		if (val == 2)
    			return SOUTH_WEST;
    		if (val == 3)
    			return SOUTH_EAST;
    		return null;
    	}

		public boolean opposite(Direction direction) {
			return (direction.val+2)%4 == val;
		}
    	
    	public Direction reverse() {
			return get((val+2)%4);
		}
		
		public String toString() {
    		if (val == 0)
    			return "NORTH_WEST";
    		if (val == 1)
    			return "NORTH_EAST";
    		if (val == 2)
    			return "SOUTH_WEST";
    		if (val == 3)
    			return "SOUTH_EAST";
    		return "";
		}
    }
    
    class MoteWrapper extends Mote {
		public MoteWrapper(Mote m) {
			super(m.getName(), m.getPosition(), m.getRadius());
			setSpeed(m.getSpeed());
		}

		
		public PointD relativeLocation(Mote o) {
			PointD myPosition = getPosition();
			PointD position = o.getPosition();
			
			return new PointD(position.x - myPosition.x,position.y - myPosition.y);
		}
		
		public Direction getQuarter(Mote o) {
			PointD location = relativeLocation(o);
			
			if (location.y >0) {
				if (location.x <0) {
					return Direction.SOUTH_WEST;
				}
				else {
					return Direction.SOUTH_EAST;
				}
			}
			else {
				if (location.x <0) {
					return Direction.NORTH_WEST;
				}
				else {
					return Direction.NORTH_EAST;
				}
			}
		}
		
		public double getProportion(Mote o) {
			return (double) (o.getRadius())/(double) (getRadius());
		}
		
		public double getDirectionMultiplier(Direction comp) {
			Direction direction = getDirection();
			
			if (comp == direction)
				return GOOD_DIRECTION_MULTIPLIER;
			else if (comp.opposite(direction))
				return BAD_DIRECTION_MULTIPLIER;
			else
				return 1;
		}
		
		public double getInercy() {
			PointD speed = getSpeed();
			return Math.sqrt(Math.pow(speed.x , 2) + Math.pow(speed.y, 2));
		}
		
		public Direction getDirection() {
			PointD dir = getSpeed();
			if (dir.y >0) {
				if (dir.x <0) {
					return Direction.SOUTH_WEST;
				}
				else {
					return Direction.SOUTH_EAST;
				}
			}
			else {
				if (dir.x <0) {
					return Direction.NORTH_WEST;
				}
				else {
					return Direction.NORTH_EAST;
				}
			}
		}
    }

}
