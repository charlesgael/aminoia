package org.gc.amino.ia.mc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.gc.amino.engine.Game;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.util.Util;

/**
 * The Class Board.
 */
public class Board {
	private Mote me;
	private List<Mote> others;
	
	private Mote me_init;
	private List<Mote> others_init;
	
	/**
	 * Instantiates a new board.
	 * 
	 * @param you
	 *            the mote the IA is playing
	 * @param others
	 *            the other motes presents on the battlefield
	 */
	public Board(Mote you, List<Mote> others) {
		this.me_init = you;
		this.others_init = others;
		others = new LinkedList<>();
	}
	
	/**
	 * Put the board in the initial configuration.
	 */
	public void reinit() {
		me = me_init.clone();
		
		if (others == null) others = new LinkedList<>();
		others.clear();
		for (Mote m : others_init) {
			others.add(m.clone());
		}
	}
	
	/**
	 * Permits to get a copy of the board as it is.
	 * 
	 * @return a copy of the board
	 */
	public Board save() {
		return new Board(me, others);
	}

	/**
	 * Checks if the game is finished.
	 * 
	 * @return true, if is finished
	 */
	public boolean isFinished() {
		return me.isDead();
	}

	/**
	 * Evaluates the score for that board.
	 * 
	 * @return the score
	 */
	public double eval() {
		double eval = me.getRadius();
		
		/*for ( Mote mote : others ) {
			if ((mote.getRadius() + radius) * 2 > me.getDistance(mote)
					&& radius < mote.getRadius())
				eval *= 0.2;
		}*/
		
		return eval;
	}

	/**
	 * Gets all the motes.
	 * 
	 * @return the motes
	 */
	public List<Mote> getMotes() {
		ArrayList<Mote> m = new ArrayList<>();
		m.add(me);
		m.addAll(others);
		return m;
	}
	
	/**
	 * Move emulates a matter ejection to propulse the controlled mote.
	 * 
	 * @param targetDirection
	 *            the direction where the matter is ejected
	 */
    public void move( PointD targetDirection ) {
    	if (targetDirection == null) return;
        if ( me.isDead() ) {
            throw new IllegalStateException( "I'm dead !" );
        }
        double angle_direction = Math.atan2( me.getPosition().y - targetDirection.y, me.getPosition().x - targetDirection.x );
        double angle_cos = Math.cos( angle_direction );
        double angle_sin = Math.sin( angle_direction );
        double new_radius = Math.sqrt( Mote.EJECTION_MASS_RATIO * Util.sqr( me.getRadius() ) ); 
        double ejected_radius = Math.sqrt( Util.sqr( me.getRadius() ) - Util.sqr( new_radius ) );
        double ejected_speed = 10;
        Mote ejected_mote = Game.createFurnitureMote( new PointD( me.getPosition().x - angle_cos * ( me.getRadius() + ejected_radius * 1.1 ),
        		me.getPosition().y - angle_sin * ( me.getRadius() + ejected_radius * 1.1 ) ),
                                                      ejected_radius );
        ejected_mote.update();  // in case it is right away eaten
        ejected_mote.setSpeed( new PointD( - angle_cos * ejected_speed, - angle_sin * ejected_speed ) );
        others.add(ejected_mote);
        me.setRadius( new_radius );
        double added_speed = ejected_speed * Util.sqr( ejected_radius ) / Util.sqr( me.getRadius() );
        me.setSpeed(new PointD(me.getSpeed().x + angle_cos * added_speed, me.getSpeed().y + angle_sin * added_speed));
    }

    /**
	 * Computes avancement.
	 */
	public void nextTurn() {
		if ( !me.isDead() ) {
            update(me);
		}
		
        Iterator<Mote> it = others.iterator();
        while ( it.hasNext() ) {
            Mote mote = it.next();
            if ( !mote.isDead() ) {
                update(mote);
            } else {
                it.remove();
            }
        }
	}	
	
    private void update(Mote m) {
    	// Compute of the new position with speed
        PointD new_position = new PointD( m.getPosition().x + m.getSpeed().x, m.getPosition().y + m.getSpeed().y );
        double mradius = m.getRadius();
        
        // Elastic bounce on terrain boundaries
        if ( new_position.x < mradius ) {
            new_position.x += ( mradius - new_position.x ) * 2;
            m.getSpeed().x *= -1;
        } else if ( new_position.x > Game.getTerrainMap().getMax().x - mradius ) {
            new_position.x -= ( new_position.x - ( Game.getTerrainMap().getMax().x - mradius ) ) * 2;
            m.getSpeed().x *= -1;
        }
        if ( new_position.y < mradius ) {
            new_position.y += ( mradius - new_position.y ) * 2;
            m.getSpeed().y *= -1;
        } else if ( new_position.y > Game.getTerrainMap().getMax().y - mradius ) {
            new_position.y -= ( new_position.y - ( Game.getTerrainMap().getMax().y - mradius ) ) * 2;
            m.getSpeed().y *= -1;
        }
        if ( ! m.getPosition().equals( new_position ) ) {
            m.setPosition(new_position);
        }
        // Eat or get eaten
        for ( Mote mote : getMotes() ) {
        	// Distance
        	double distance = m.getDistance( mote );
            if ( distance < mradius + mote.getRadius() ) {
                // simple quadratic equation solving transfer of surface from smaller to larger and point opposite to contact not changing 
                double foo = ( mradius + mote.getRadius() + distance ) / 2;
                double a = 2;
                double b = - 2 * foo;
                double c = Util.sqr( foo ) - Util.sqr( mradius ) - Util.sqr( mote.getRadius() );
                double new_radius = ( - b + Math.sqrt( Util.sqr( b ) - 4 * a * c ) ) / ( 2 * a );
                double other_new_radius = foo - new_radius;
                double new_radius_larger = Util.maxd( new_radius, other_new_radius );
                double new_radius_smaller = Util.mind( new_radius, other_new_radius );
                if ( new_radius_smaller <= 0 ) {
                    new_radius_larger = Math.sqrt( Util.sqr( mradius ) + Util.sqr( mote.getRadius() ) );
                    new_radius_smaller = 0;
                }
                if ( mradius > mote.getRadius() ) {
                    // eat
                    eat( m, mote, new_radius_larger, new_radius_smaller );
                } else {
                    // get eaten
                    eat( mote, m, new_radius_larger, new_radius_smaller );
                }
            }
        }
    }

	private void eat(Mote larger, Mote smaller, double new_radius_larger, double new_radius_smaller) {
        double angle_direction = Math.atan2( larger.getPosition().y - smaller.getPosition().y, larger.getPosition().x - smaller.getPosition().x );
        // while growing from radius to new_radius, absorb momentum thus amend speed
        double added_speed_factor = Util.sqr( smaller.getRadius() - new_radius_smaller ) / Util.sqr( larger.getRadius() );
        larger.getSpeed().x = larger.getSpeed().x * Util.sqr( larger.getRadius() ) / Util.sqr( new_radius_larger )
                              + smaller.getSpeed().x * added_speed_factor;
        larger.getSpeed().y = larger.getSpeed().y * Util.sqr( larger.getRadius() ) / Util.sqr( new_radius_larger )
                              + smaller.getSpeed().y * added_speed_factor;
        larger.getPosition().x += ( larger.getRadius() - new_radius_larger ) * Math.cos( angle_direction );
        larger.getPosition().y += ( larger.getRadius() - new_radius_larger ) * Math.sin( angle_direction );
        larger.setRadius( new_radius_larger );
        smaller.getPosition().x += ( new_radius_smaller - smaller.getRadius() ) * Math.cos( angle_direction );
        smaller.getPosition().y += ( new_radius_smaller - smaller.getRadius() ) * Math.sin( angle_direction );
        smaller.setRadius( new_radius_smaller );
        if ( new_radius_smaller == 0 ) {
            smaller.die();
        }
	}

	/**
	 * Gets the IA controlled mote.
	 * 
	 * @return IA controlled mote
	 */
	public Mote getMe() {
		return me;
	}
}
