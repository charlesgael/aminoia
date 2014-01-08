package org.gc.amino.engine.mote;

import org.apache.log4j.Logger;
import org.gc.amino.engine.Game;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.util.Triplet;
import org.gc.amino.util.Util;

public class Mote {
    
    private static final Logger log = Logger.getLogger( Mote.class );

    public static final double EJECTION_MASS_RATIO = 0.995;
    
    private String name;
    private Triplet<Integer, Integer, Integer> color;
    private PointD position;
    private PointD speed;
    private double radius;
    private boolean dead = false;

    public Mote( String name, PointD position, double radius ) {
        this.name = name;
        this.position = position;
        this.radius = radius;
        this.speed = new PointD( 0, 0 );
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public void setColor( int r, int g, int b ) {
        this.color = new Triplet<Integer, Integer, Integer>( r, g, b );
    }
    
    public void die() {
        dead = true;
    }
    public boolean isDead() {
        return dead;
    }
    
    public String getName() {
        return name;
    }
    
    public PointD getPosition() {
        return position;
    }
    public void setPosition( PointD p ) {
    	position = p;
    }
    
    public double getRadius() {
        return radius;
    }
    public void setRadius( double radius ) {
        this.radius = radius;
    }
    
    public PointD getSpeed() {
        return speed;
    }
    public void setSpeed( PointD speed ) {
        this.speed = speed;
    }

    public double getDistance( Mote otherMote ) {
        return Math.sqrt( Util.sqr( position.x - otherMote.getPosition().x ) + Util.sqr( position.y - otherMote.getPosition().y ) );
    }
    
    /** Perform a move e.g. eject mass in said direction */
    public void move( PointD targetDirection ) {
        if ( isDead() ) {
            throw new IllegalStateException( "dead" );
        }
        double angle_direction = Math.atan2( position.y - targetDirection.y, position.x - targetDirection.x );
        double angle_cos = Math.cos( angle_direction );
        double angle_sin = Math.sin( angle_direction );
        double new_radius = Math.sqrt( EJECTION_MASS_RATIO * Util.sqr( radius ) ); 
        double ejected_radius = Math.sqrt( Util.sqr( radius ) - Util.sqr( new_radius ) );
        double ejected_speed = 10;
        Mote ejected_mote = Game.createFurnitureMote( new PointD( position.x - angle_cos * ( radius + ejected_radius * 1.1 ),
                                                                  position.y - angle_sin * ( radius + ejected_radius * 1.1 ) ),
                                                      ejected_radius );
        ejected_mote.update();  // in case it is right away eaten
        ejected_mote.setSpeed( new PointD( - angle_cos * ejected_speed, - angle_sin * ejected_speed ) );
        Game.addMote( ejected_mote );
        radius = new_radius;
        double added_speed = ejected_speed * Util.sqr( ejected_radius ) / Util.sqr( radius );
        speed.x += angle_cos * added_speed;
        speed.y += angle_sin * added_speed;
    }
    
    public Triplet<Integer, Integer, Integer> getColor() {
        return color;
    }
    
    public String toString() {
        return "{" + name + "/" + position + "/" + radius + "/" + speed + "}"; 
    }

    public void update() {
        PointD new_position = new PointD( position.x + speed.x, position.y + speed.y );
        // elastic bounce on terrain boundaries
        if ( new_position.x < radius ) {
            new_position.x += ( radius - new_position.x ) * 2;
            speed.x *= -1;
        } else if ( new_position.x > Game.getTerrainMap().getMax().x - radius ) {
            new_position.x -= ( new_position.x - ( Game.getTerrainMap().getMax().x - radius ) ) * 2;
            speed.x *= -1;
        }
        if ( new_position.y < radius ) {
            new_position.y += ( radius - new_position.y ) * 2;
            speed.y *= -1;
        } else if ( new_position.y > Game.getTerrainMap().getMax().y - radius ) {
            new_position.y -= ( new_position.y - ( Game.getTerrainMap().getMax().y - radius ) ) * 2;
            speed.y *= -1;
        }
        if ( ! position.equals( new_position ) ) {
            position = new_position;
        }
        // eat or get eaten
        for ( Mote mote : Game.getMotes() ) {
            if ( mote == this ) {
                continue;
            }
            double distance = getDistance( mote );
            if ( distance < radius + mote.getRadius() ) {
                // simple quadratic equation solving transfer of surface from smaller to larger and point opposite to contact not changing 
                double foo = ( radius + mote.getRadius() + distance ) / 2;
                double a = 2;
                double b = - 2 * foo;
                double c = Util.sqr( foo ) - Util.sqr( radius ) - Util.sqr( mote.getRadius() );
                double new_radius = ( - b + Math.sqrt( Util.sqr( b ) - 4 * a * c ) ) / ( 2 * a );
                double other_new_radius = foo - new_radius;
                double new_radius_larger = Util.maxd( new_radius, other_new_radius );
                double new_radius_smaller = Util.mind( new_radius, other_new_radius );
                if ( new_radius_smaller <= 0 ) {
                    new_radius_larger = Math.sqrt( Util.sqr( radius ) + Util.sqr( mote.getRadius() ) );
                    new_radius_smaller = 0;
                }
                if ( radius > mote.getRadius() ) {
                    // eat
                    eat( this, mote, new_radius_larger, new_radius_smaller );
                } else {
                    // get eaten
                    eat( mote, this, new_radius_larger, new_radius_smaller );
                }
            }
        }
    }
    
    private static void eat( Mote larger, Mote smaller, double new_radius_larger, double new_radius_smaller ) {
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
    
    public boolean equals( Object otherObject ) {
        if ( otherObject instanceof Mote ) {
            return getName().equals( ( (Mote)otherObject ).getName() );
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
	public Mote(Mote mote) {
        this.name = mote.name;
        this.position = new PointD( mote.position.x, mote.position.y );
        this.radius = mote.radius;
        this.speed = new PointD( mote.speed.x, mote.speed.y );
	}
	
	public Mote clone() {
		return new Mote(this);
	}
}
