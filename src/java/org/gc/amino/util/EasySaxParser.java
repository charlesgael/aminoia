package org.gc.amino.util;

import java.io.InputStream;
import java.util.Map;

public class EasySaxParser {

    /** Listener to implement to receive parsed data. */
    public interface Listener {
        /** Method triggered on an opening XML element. */
        public void startElement( String name, Map<String, String> attributes );
        /** Method triggered on an closing XML element. */
        public void endElement( String name, String cdata );
    }
    
    /**
     * Parse the passed inputstream and use the listener to provide data.
     * Return null on parse success, an error key on parse error.
     */
    public static String parse( InputStream in, Listener listener ) {
        return new EasySaxParserInternal().parse( in, listener );
    }

}
