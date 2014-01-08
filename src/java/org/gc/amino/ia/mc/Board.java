package org.gc.amino.ia.mc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.gc.amino.engine.Game;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.util.Util;

public class Board {
	private Mote me;
	private List<Mote> others;
	
	private Mote me_init;
	private List<Mote> others_init;
	
	private LinkedList<Action> actions;
	
	public Board(Mote you, List<Mote> others) {
		this.me_init = you;
		this.others_init = others;

		others = new LinkedList<>();
	}
	
	public void reinit() {
		me = me_init.clone();
		
		if (others == null) others = new LinkedList<>();
		others.clear();
		for (Mote m : others_init) {
			others.add(m.clone());
		}
	}
	
	public Board save() {
		return new Board(me, others);
	}

	public boolean isFinished() {
		return me.isDead();
	}

	public double eval() {
		double radius = me.getRadius();
		double eval = radius;
		
		for ( Mote mote : others ) {
			if ((mote.getRadius() + radius) * 2 > me.getDistance(mote)
					&& radius < mote.getRadius())
				eval *= 0.2;
		}
		
		return eval;
	}

	public List<Mote> getMotes() {
		ArrayList<Mote> m = new ArrayList<>();
		m.add(me);
		m.addAll(others);
		return m;
	}
    public void move( PointD targetDirection ) {
    	if (targetDirection == null) return;
        if ( me.isDead() ) {
            throw new IllegalStateException( "dead" );
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

	public void nextTurn() {
		if ( ! me.isDead() ) {
            update(me);
		}
		
        Iterator<Mote> it = others.iterator();
        while ( it.hasNext() ) {
            Mote mote = it.next();
            if ( ! mote.isDead() ) {
                update(mote);
            } else {
                it.remove();
            }
        }
	}
	
    public void update(Mote m) {
        PointD new_position = new PointD( m.getPosition().x + m.getSpeed().x, m.getPosition().y + m.getSpeed().y );
        // elastic bounce on terrain boundaries
        if ( new_position.x < m.getRadius() ) {
            new_position.x += ( m.getRadius() - new_position.x ) * 2;
            m.getSpeed().x *= -1;
        } else if ( new_position.x > Game.getTerrainMap().getMax().x - m.getRadius() ) {
            new_position.x -= ( new_position.x - ( Game.getTerrainMap().getMax().x - m.getRadius() ) ) * 2;
            m.getSpeed().x *= -1;
        }
        if ( new_position.y < m.getRadius() ) {
            new_position.y += ( m.getRadius() - new_position.y ) * 2;
            m.getSpeed().y *= -1;
        } else if ( new_position.y > Game.getTerrainMap().getMax().y - m.getRadius() ) {
            new_position.y -= ( new_position.y - ( Game.getTerrainMap().getMax().y - m.getRadius() ) ) * 2;
            m.getSpeed().y *= -1;
        }
        if ( ! m.getPosition().equals( new_position ) ) {
            m.setPosition(new_position);
        }
        // eat or get eaten
        for ( Mote mote : getMotes() ) {
            if ( mote == m || (mote.getRadius() + m.getRadius()) * 1.5 < m.getDistance(mote)) {
                continue;
            }
            double distance = m.getDistance( mote );
            if ( distance < m.getRadius() + mote.getRadius() ) {
                // simple quadratic equation solving transfer of surface from smaller to larger and point opposite to contact not changing 
                double foo = ( m.getRadius() + mote.getRadius() + distance ) / 2;
                double a = 2;
                double b = - 2 * foo;
                double c = Util.sqr( foo ) - Util.sqr( m.getRadius() ) - Util.sqr( mote.getRadius() );
                double new_radius = ( - b + Math.sqrt( Util.sqr( b ) - 4 * a * c ) ) / ( 2 * a );
                double other_new_radius = foo - new_radius;
                double new_radius_larger = Util.maxd( new_radius, other_new_radius );
                double new_radius_smaller = Util.mind( new_radius, other_new_radius );
                if ( new_radius_smaller <= 0 ) {
                    new_radius_larger = Math.sqrt( Util.sqr( m.getRadius() ) + Util.sqr( mote.getRadius() ) );
                    new_radius_smaller = 0;
                }
                if ( m.getRadius() > mote.getRadius() ) {
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

	public Mote getMe() {
		return me;
	}
}
