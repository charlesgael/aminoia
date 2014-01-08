package org.gc.amino.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.engine.terrainmap.TerrainMap;
import org.gc.amino.util.Util;

public class Game {
    
    private static final Logger log = Logger.getLogger( Game.class );

    private static TerrainMap terrainMap;
    private static List<Mote> motes = new ArrayList<Mote>();
    private static List<ExternalController> externalControllers = new ArrayList<ExternalController>();
    
    public static void init( TerrainMap tMap ) {
        terrainMap = tMap;
    }

    public static TerrainMap getTerrainMap() {
        return terrainMap;
    }

    public static Mote createFurnitureMote( PointD position, double radius ) {
        Mote mote = new Mote( "furniture", position, radius );
        mote.setColor( 200, 130, 200 );
        return mote;
    }
    
    public static void addMote( Mote mote ) {
        mote.setName( mote.getName() + "#" + motes.size() );
        motes.add( mote );
    }

    public static void addExternallyControlledMote( Mote mote, String url ) {
        motes.add( mote );
        try {
            externalControllers.add( new ExternalController( mote, url ) );
        } catch ( Exception e ) {
            log.error( "Failed to add IA at URL " + url + ": " + e );
            mote.setColor( 120, 120, 120 );
        } 
    }

    public static void iterateGame() {
        StringBuilder xml_state = new StringBuilder();
        xml_state.append( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" )
                 .append( "<frame>\n" );
        for ( Mote mote : motes ) {
            xml_state.append( "  <mote pos='" ).append( mote.getPosition() ).append( "' radius='" ).append( mote.getRadius() )
                     .append( "' speed='" ).append( mote.getSpeed() ).append( "' name='" ).append( mote.getName() ).append( "'" );
            if ( mote.isDead() ) {
                xml_state.append( " dead='true'" );
            }
            xml_state.append( "/>\n" );
        }
        xml_state.append( "</frame>" );
        String xml_state_s = xml_state.toString();
        for ( ExternalController external_controller : externalControllers ) {
            if ( ! external_controller.isDead() ) {
                external_controller.update( xml_state_s );
            }
        }
        Iterator<Mote> it = getMotes().iterator();
        while ( it.hasNext() ) {
            Mote mote = it.next();
            if ( ! mote.isDead() ) {
                mote.update();
            } else {
                it.remove();
            }
        }
    }

    public static List<Mote> getMotes() {
        return motes;
    }
        
    public static Mote getMoteAt( int x, int y ) {
        for ( Mote mote : getMotes() ) {
            if ( Util.sqr( x - mote.getPosition().x ) + Util.sqr( y - mote.getPosition().y ) < Util.sqr( mote.getRadius() ) ) {
                return mote;
            }
        }
        return null;
    }

}
