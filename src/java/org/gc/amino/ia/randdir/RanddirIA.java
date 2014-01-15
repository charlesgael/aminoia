package org.gc.amino.ia.randdir;

import java.io.IOException;
import java.util.List;

import org.gc.amino.engine.Game;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.engine.terrainmap.TerrainMap;
import org.gc.amino.ia.httpserver.IaDeliveryInterface;
import org.gc.amino.ia.httpserver.IaLauncher;
import org.gc.amino.ia.mc.MontecarloIa;

/**
 * The Class RanddirIA.
 * 
 * That IA simulates random directions at the start of the game and checks
 * weather it is productive or not.
 * It then selects the best direction and stick to it.
 */
public class RanddirIA implements IaDeliveryInterface {

	/**
	 * The launcher of the Randdir IA.
	 * 
	 * @param argv
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main( String[] argv ) throws IOException {
		IaLauncher.launch( argv, new MontecarloIa() );
	}

	/* (non-Javadoc)
	 * @see org.gc.amino.ia.httpserver.IaDeliveryInterface#init(org.gc.amino.engine.terrainmap.PointD)
	 */
	@Override
	public void init(PointD size) {
		Game.init(new TerrainMap(size));
		//actions = new LinkedList<>();
	}

	/* (non-Javadoc)
	 * @see org.gc.amino.ia.httpserver.IaDeliveryInterface#frame(org.gc.amino.engine.mote.Mote, java.util.List)
	 */
	@Override
	public PointD frame(Mote you, List<Mote> otherMotes) {
		return null;
	}
}
