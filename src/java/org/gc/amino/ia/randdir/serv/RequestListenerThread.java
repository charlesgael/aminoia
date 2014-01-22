package org.gc.amino.ia.randdir.serv;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.modelmbean.RequiredModelMBean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.ia.httpserver.IaDeliveryInterface;
import org.gc.amino.ia.randdir.ui.RanddirUI;
import org.gc.amino.util.DataHolder;
import org.gc.amino.util.EasySaxParser;
import org.w3c.dom.Attr;

public class RequestListenerThread extends Thread {
	private ServerSocket serverSocket;
	private SyncBasicHttpParams params;
	private RanddirUI ui;
	private HttpService httpService;
	private MyHttpRequestHandler httpRequestaHandler;
	private Class<? extends IaDeliveryInterface> iaClass;
	private HttpRequestHandlerRegistry reqistry;

	public RequestListenerThread(RanddirUI randdirUI, int port, Class<? extends IaDeliveryInterface> ia) throws IOException {
		iaClass = ia;
		ui = randdirUI;
		serverSocket = new ServerSocket(port);
        params = new SyncBasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
        
        setupService();
	}
	
	private void setupService() {
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
        });

        // Set up request handlers
        HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
        try {
			reqistry.register("*", httpRequestaHandler = new MyHttpRequestHandler( iaClass.newInstance() ) );
		} catch (Exception e) {}
        
        // Set up the HTTP service
        httpService = new HttpService( httpproc,
                                            new DefaultConnectionReuseStrategy(),
                                            new DefaultHttpResponseFactory(),
                                            reqistry,
                                            params);
		
	}
	
	public void run() {
		ui.setStatus("Listening on port " + serverSocket.getLocalPort());
        while (!Thread.interrupted()) {
            try {
                // Set up HTTP connection
                Socket socket = serverSocket.accept();
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                conn.bind(socket, params);

                // Start worker thread
                Thread t = new InstanceIA(this.httpService, conn, httpRequestaHandler, ui);
                t.setDaemon(true);
                t.start();
            } catch (InterruptedIOException ex) {
                break;
            } catch (IOException e) {
                System.err.println("I/O error initialising connection thread: "
                        + e.getMessage());
                break;
            }
        }
	}
	
	public void reset() {
		try {
			httpRequestaHandler.reset();
			httpRequestaHandler.iaDeliveryInterface = iaClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {}
	}
	
	
	
    static class MyHttpRequestHandler implements HttpRequestHandler {

        private String myName = null;
        private IaDeliveryInterface iaDeliveryInterface;
        
        public MyHttpRequestHandler( IaDeliveryInterface iaDeliveryInterface ) {
            this.iaDeliveryInterface = iaDeliveryInterface;
        }
        
        public void reset() {
            myName = null;
        }
        
        public void handle( final HttpRequest request,
                            final HttpResponse response,
                            final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            if ( ! ( request instanceof HttpEntityEnclosingRequest ) ) {
                throw new IllegalArgumentException( "must receive POST content" );
            }
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if ( myName == null ) {
                // parse init
                String error = EasySaxParser.parse( entity.getContent(), new EasySaxParser.Listener() {
                    public void startElement( String name, Map<String, String> attributes ) {
                    	/*System.out.print("<"+name);
                    	for(Entry<String, String> attribute : attributes.entrySet()){
                    		System.out.print(" "+attribute.getKey()+"=\""+attribute.getValue()+"\"");
                    	}
                    	System.out.println(" />");*/
                    	
                        if ( name.equals( "init" ) ) {
                            myName = attributes.get( "you" );
                            String[] components = attributes.get( "size" ).split( "x" );
                            PointD size = new PointD( Double.parseDouble( components[ 0 ] ),
                                                      Double.parseDouble( components[ 1 ] ) );
                            iaDeliveryInterface.init( size );
                        }
                    }
                    public void endElement( String name, String cdata ) {}
                } );
                if ( error != null ) {
                    throw new IllegalArgumentException( "XML parse error " + error );
                }
                if ( myName == null ) {
                    throw new IllegalArgumentException( "Unexpectedly did not found init you attribute in <init> root element in received data" );
                }
                response.setStatusCode(HttpStatus.SC_OK);
                
            } else {
                // parse frame
                final DataHolder<Mote> me = new DataHolder<Mote>();
                final List<Mote> motes = new ArrayList<Mote>();
                String error = EasySaxParser.parse( entity.getContent(), new EasySaxParser.Listener() {
                    public void startElement( String name, Map<String, String> attributes ) {
                    	/*System.out.print("<"+name);
                    	for(Entry<String, String> attribute : attributes.entrySet()){
                    		System.out.print(" "+attribute.getKey()+"=\""+attribute.getValue()+"\"");
                    	}
                    	System.out.println(" />");*/
                    	
                        if ( name.equals( "mote" ) ) {
                            String[] components = attributes.get( "pos" ).split( "x" );
                            PointD pos = new PointD( Double.parseDouble( components[ 0 ] ),
                                                     Double.parseDouble( components[ 1 ] ) );
                            components = attributes.get( "speed" ).split( "x" );
                            PointD speed = new PointD( Double.parseDouble( components[ 0 ] ),
                                                       Double.parseDouble( components[ 1 ] ) );
                            double radius = Double.parseDouble( attributes.get( "radius" ) );
                            String name_ = attributes.get( "name" );
                            boolean dead = attributes.containsKey( "dead" );
                            Mote mote = new Mote( name_, pos, radius );
                            mote.setSpeed( speed );
                            if ( dead ) {
                                mote.die();
                            }
                            if ( name_.equals( myName ) ) {
                                me.data = mote;
                            } else {
                                motes.add( mote );
                            }
                        }
                    }
                    public void endElement( String name, String cdata ) {}
                } );
                if ( error != null ) {
                    throw new IllegalArgumentException( "XML parse error " + error );
                }
                if ( me.data == null ) {
                    throw new IllegalArgumentException( "Unexpectedly did not found self mote in received data" );
                }
                PointD move = iaDeliveryInterface.frame( me.data, motes );
                String resp = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"; 
                if ( move == null ) {
                    resp += "<nothing/>";
                } else {
                    resp += "<move direction='" + move.x + "x" + move.y + "'/>";
                }
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity( new StringEntity( resp ) ) ;
            }
        }
    }
}