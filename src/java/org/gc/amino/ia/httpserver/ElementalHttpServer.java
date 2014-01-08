/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * http://hc.apache.org/httpcomponents-core-ga/httpcore/examples/org/apache/http/examples/ElementalHttpServer.java
 * 
 */

package org.gc.amino.ia.httpserver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
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
import org.apache.log4j.Logger;
import org.gc.amino.engine.mote.Mote;
import org.gc.amino.engine.terrainmap.PointD;
import org.gc.amino.util.DataHolder;
import org.gc.amino.util.EasySaxParser;

/**
 * Based on:
 * 
 *   http://hc.apache.org/httpcomponents-core-ga/httpcore/examples/org/apache/http/examples/ElementalHttpServer.java
 *
 *   Basic, yet fully functional and spec compliant, HTTP/1.1 file server.
 *   <p>
 *   Please note the purpose of this application is demonstrate the usage of HttpCore APIs.
 *   It is NOT intended to demonstrate the most efficient way of building an HTTP file server.
 *   
 * modified to be used by amino IAs
 * 
 */
public class ElementalHttpServer {

    private static final Logger log = Logger.getLogger( ElementalHttpServer.class );
    
    public static class RequestListenerThread extends Thread {

        private final ServerSocket serversocket;
        private final HttpParams params;
        private final HttpService httpService;
        private MyHttpRequestHandler httpRequestaHandler;

        public RequestListenerThread( int port, IaDeliveryInterface iaDeliveryInterface ) throws IOException {
            this.serversocket = new ServerSocket(port);
            this.params = new SyncBasicHttpParams();
            this.params
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                    new ResponseDate(),
                    new ResponseServer(),
                    new ResponseContent(),
                    new ResponseConnControl()
            });

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", httpRequestaHandler = new MyHttpRequestHandler( iaDeliveryInterface ) );

            // Set up the HTTP service
            this.httpService = new HttpService( httpproc,
                                                new DefaultConnectionReuseStrategy(),
                                                new DefaultHttpResponseFactory(),
                                                reqistry,
                                                this.params);
        }

        @Override
        public void run() {
            log.info("Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    log.info("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, this.params);

                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn, httpRequestaHandler);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    log.error("I/O error initialising connection thread: "
                            + e.getMessage());
                    break;
                }
            }
        }
    }

    private static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;
        private MyHttpRequestHandler httpRequestHandler;

        public WorkerThread( final HttpService httpservice,
                             final HttpServerConnection conn,
                             MyHttpRequestHandler httpRequestaHandler ) { 
            super();
            this.httpservice = httpservice;
            this.conn = conn;
            this.httpRequestHandler = httpRequestaHandler;
        }

        @Override
        public void run() {
            log.info("New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                log.info("Client closed connection");
                httpRequestHandler.reset();
            } catch (IOException ex) {
                log.warn("I/O error: " + ex);
            } catch (HttpException ex) {
                log.error("Unrecoverable HTTP protocol violation: " + ex);
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }

    }

    private static class MyHttpRequestHandler implements HttpRequestHandler {

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
        
