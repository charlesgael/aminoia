package org.gc.amino.engine.terrainmap;

import org.apache.log4j.Logger;

public class TerrainMap {
    
    private static final Logger log = Logger.getLogger( TerrainMap.class );

    private PointD max;
    
    public TerrainMap( PointD max ) {
        this.max = max;
    }

    public PointD getMax() {
        return max;
    }

}
