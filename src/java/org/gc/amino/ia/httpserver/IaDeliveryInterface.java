package org.gc.amino.ia.httpserver;

import java.util.List;

import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;

public interface IaDeliveryInterface {
    /** Called on game initialization with terrain size. */
    public void init( PointD size );
    /** Called on all game frames. Should return a mass ejection direction for a move, or null for no move. */
    public PointD frame( Mote you, List<Mote> otherMotes );
}
