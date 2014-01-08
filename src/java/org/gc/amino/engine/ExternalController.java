package org.gc.amino.engine;

import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.util.EasySaxParser;

public class ExternalController {
    
    private static final Logger log = Logger.getLogger( Game.class );

    private static PoolingClientConnectionManager connectionManager;
    
    private HttpClient client;
    private String url;
    private Mote mote;
    
    static {
        connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal( 20 );
        connectionManager.setDefaultMaxPerRoute( 20 );
    }
    
    public ExternalController( Mote mote, String url ) {
        client = new DefaultHttpClient( connectionManager );
        ( (DefaultHttpClient) client ).setKeepAliveStrategy( new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration( HttpResponse response, HttpContext context ) {
                return 30 * 1000;
            }
        } );
        HttpConnectionParams.setConnectionTimeout( client.getParams(), 10000 );
        HttpConnectionParams.setSoTimeout( client.getParams(), 20000 );
        this.mote = mote;
        this.url = url;
        
        StringBuilder xml_init = new StringBuilder();
        xml_init.append( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" )
                .append( "<init size='" ).append( Game.getTerrainMap().getMax() )
                .append( "' you='" ).append( mote.getName() ).append( "'/>" );
        HttpPost post = new HttpPost( url );
        post.setEntity( new StringEntity( xml_init.toString(), ContentType.create( "text/xml", Consts.UTF_8 ) ) );
        try {
            client.execute( post );
        } catch ( Exception e ) {
            post.abort();
            throw new IllegalArgumentException( "first request failure: " + e );
        }
    }
    
    public boolean isDead() {
        return mote.isDead();
    }
    
    public void update( String xmlState ) {
        HttpPost post = new HttpPost( url );
        post.setEntity( new StringEntity( xmlState, ContentType.create( "text/xml", Consts.UTF_8 ) ) );
        HttpResponse response = null;
        try {
            response = client.execute( post );
            String error = EasySaxParser.parse( response.getEntity().getContent(), new EasySaxParser.Listener() {
                public void startElement( String name, Map<String, String> attributes ) {
                    if ( name.equals( "move" ) ) {
                        String[] components = attributes.get( "direction" ).split( "x" );
                        mote.move( new PointD( Double.parseDouble( components[ 0 ] ),
                                               Double.parseDouble( components[ 1 ] ) ) );
                    }
                }
                public void endElement( String name, String cdata ) {}
            } );
            if ( error != null ) {
                throw new IllegalArgumentException( "XML parse error " + error );
            }
        } catch ( Exception e ) {
            post.abort();
            log.error( "failure: " + e );
        }
        
    }


}
