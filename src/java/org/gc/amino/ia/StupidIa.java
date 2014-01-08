package org.gc.amino.ia;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.ia.httpserver.IaDeliveryInterface;
import org.gc.amino.ia.httpserver.IaLauncher;

/**
 * This IA goes erratically towards nonsense directions.
 * It is only a simple proof of concept of an IA. 
 */
public class StupidIa implements IaDeliveryInterface {

    private static final Logger log = Logger.getLogger( StupidIa.class );

    private Random random = new Random();
    private PointD terrainSize;
    private PointD currentDirection = null;
    private int moving_counter = 0;
    
    @Override
    public void init( PointD size ) {
        terrainSize = size;
    }
    
    @Override
    public PointD frame( Mote you, List<Mote> otherMotes ) {
        if ( moving_counter > 0 ) {
            moving_counter--;
            return currentDirection;
        } else if ( random.nextDouble() < 0.02 ) {
            moving_counter = 4;
            currentDirection = new PointD( random.nextInt( (int)terrainSize.x ), random.nextInt( (int)terrainSize.y ) );
            log.info( "New current direction " + currentDirection ); 
            return currentDirection;
        } else {
            return null;
        }
    }

    public static void main( String[] argv ) throws IOException {
        IaLauncher.launch( argv, new StupidIa() );
    }

}
