package org.gc.amino.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.gc.amino.util.EasySaxParser.Listener;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Do not use this class directly! */
public class EasySaxParserInternal extends DefaultHandler {

    private static final Logger log = Logger.getLogger( EasySaxParserInternal.class );
            
    private static final Map<String, String> EMPTY_ATTRIBUTES = new HashMap<String, String>();
    
    /** The XML parser instance. */
    private XMLReader mParser;
    /** The string holding the current character string. */
    private StringBuilder mCurrentCdata = new StringBuilder();
    /** The listener that will receive data. */
    private Listener mListener;
   
    /** Do not use this class directly! */
    String parse( InputStream in, Listener listener ) {
        mListener = listener;
        try {
            mParser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            mParser.setContentHandler( this );
            mParser.setErrorHandler( this );
            mParser.setFeature( "http://xml.org/sax/features/validation", false );
            
        } catch ( Exception e ) {
            log.error( "Can't create XML parser: " + e );
            return "internalerror";
        }
        try {
            mParser.parse( new InputSource( in ) );
            return null;
        } catch ( Exception e ) {
            log.info( "Error while parsing XML data in " + getClass().getName() + ": " + Util.printException( e ) );
            return e.getMessage(); 
        }
    }

    private static Map<String, String> attributes2map( Attributes attributes ) {
        final int length = attributes.getLength();
        if ( length == 0 ) {
            // optimized path
            return EMPTY_ATTRIBUTES;
        }
        Map<String, String> map = new HashMap<String, String>( length );
        for ( int i = 0; i < length; i++ ) {
            map.put( attributes.getLocalName( i ), attributes.getValue( i ) );
        }
        return map;
    }
    
    /** Method called by the parser whenever a starting tag is encountered. */
    public void startElement( String uri, String localName, String qName, Attributes attributes )
            throws SAXException {
        mCurrentCdata = new StringBuilder();
        mListener.startElement( qName, attributes2map( attributes ) );
    }

    /** Method called by the parser whenever an ending tag is encountered. */
    public void endElement( String uri, String localName, String qName ) throws SAXException {
        mListener.endElement( qName, mCurrentCdata.toString().trim() );
        mCurrentCdata = new StringBuilder();
    }

    /** Method called by the parser whenever CDATA are encountered. */
    public void characters( char[] ch, int start, int length ) {
        mCurrentCdata.append( ch, start, length );
    }

    /** Method called by the parser whenever a warning message is generated by the parser. */
    public void warning( SAXParseException ex ) {
        log.info( "warning in parsing: " + ex.getMessage() );
    }

    /** Method called by the parser whenever an error is generated by the parser. */
    public void error( SAXParseException ex ) {
        log.info( "error in parsing: " + ex.getMessage() );
    }

    /**
     * Method called by the parser whenever a fatal error is encountered by the parser.
     * After this call, the parser will not continue parsing, but will return from the parse() method.
     */
    public void fatalError( SAXParseException ex ) {
        log.info( "fatal in parsing: " + ex.getMessage() );
    }

}
