package org.gc.amino.ia.httpserver;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.gc.amino.ia.httpserver.ElementalHttpServer.RequestListenerThread;

public class IaLauncher {
    
    public static void launch( String[] argv, IaDeliveryInterface iaDeliveryInterface ) throws IOException {

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel( Level.INFO );
        PatternLayout pa = new PatternLayout();
        pa.setConversionPattern( "%d{ISO8601} %-5p %c{1}:%L(%M) - %m%n" );
        ( (Appender) Logger.getRootLogger().getAllAppenders().nextElement() ).setLayout( pa );

        int port = 1234;
        // you may optionally specify on commandline the port to use
        if ( argv.length == 1 ) {
            port = Integer.parseInt( argv[0] );
        }
        Thread t = new RequestListenerThread( port, iaDeliveryInterface );
        t.setDaemon(false);
        t.start();
    }
    
}
