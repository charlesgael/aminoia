/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Open Shopping List is licensed under the Apache 2.0 license.
 *
 */

package org.gc.amino.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Util {

    private static final Logger log = Logger.getLogger( Util.class );

    /** Convert an unsigned byte to an int. */ 
    public static int unsignedByteToInt( byte input ) {
        return input < 0 ? input + 256 : input; 
    }

    /**
     * Join the elements of the Collection into a String using the
     * provided separator, with a maximum elements. The type describes how
     * to retrieve the String representation of each element.
     */
    public static <T> String join( Collection<T> col, String sep, int max, String ellipsizeText, String type ) {
        StringBuilder sb = new StringBuilder();
        Iterator<T> li = col.iterator();
        if ( li.hasNext() ) {
            if ( type.equals( "toString" ) ) {
                sb.append( li.next() );
            } else if ( type.equals( "getClass" ) ) {
                sb.append( li.next().getClass().getName() );
            } else {
                sb.append( "???" );
            }
            while ( li.hasNext() ) {
                if ( max == 1 ) {
                    sb.append( ellipsizeText );
                    return sb.toString();
                }
                sb.append( sep );
                if ( type.equals( "toString" ) ) {
                    sb.append( li.next() );
                } else if ( type.equals( "getClass" ) ) {
                    sb.append( li.next().getClass().getName() );
                } else {
                    sb.append( "???" );
                }
                max--;
            }
        }
        return sb.toString();
    }
    
    /**
     * Join the elements of the Collection into a String using the
     * provided separator.
     */
    public static <T> String join( Collection<T> col, String sep ) {
        return join( col, sep, -1, null, "toString" );
    }
   
    /**
     * Retrieve the stacktrace by sending an exception, catching it
     * and getting its stacktrace.
     */
    public static StackTraceElement[] getStackTrace() {
        return new Exception().fillInStackTrace().getStackTrace();
    }

    /**
     * Returns the full backtrace corresponding to the StackTraceElement's
     * passed. This is useful to get the backtrace from a catch block.
     */
    public static String backtraceFull( StackTraceElement[] trace, int from ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = from; i < trace.length; i++ ) {
            sb.append( "\t" ).append( trace[ i ].toString() ).append( "\n" );
        }
        return sb.toString();
    }

    /**
     * Returns the full backtrace of the caller method.
     */
    public static String backtraceFull() {
        return backtraceFull( getStackTrace(), 2 );
    }
 
    /** Go up the chain of exception causes, and print a nice trace of that all. */ 
    public static String printException( Exception e ) { 
        StringBuilder out = new StringBuilder();
        String causePrefix = "";
        Throwable t = e;
        while ( t != null ) {
            out.append( causePrefix )
               .append( "exception: " )
               .append( t )
               .append( " at: " );
            if ( t.getCause() == null ) {
                // no more cause, print full backtrace now because that's the most interesting exception
                out.append( "\n" )
                   .append( backtraceFull( t.getStackTrace(), 0 ) )
                   .append( "\n" );
            } else {
                // there's a cause, print only one line of trace because that is not the most interesting exception
                out.append( t.getStackTrace()[ 0 ] )
                   .append( "\n" );                    
            }
            t = t.getCause();
            // grow the cause prefix 
            causePrefix += "...cause: ";
        }
        return out.toString();
    }
 
    /** Create a List (ArrayList) with any number of values. */
    public static <T> List<T> newList( T... values ) {
        return new ArrayList<T>( Arrays.asList( values ) );
    }
   
    public static char[] hexDigits
        = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * Notice: copied from commons-codec-1.2's Hex.encodeHex, in order to have uppercase hexDigits
     * directly instead of the need to call #toUpperCase.
     * 
     * Converts an array of bytes into an array of characters representing the
     * hexadecimal values of each byte in order. The returned array will be
     * double the length of the passed array, as it takes two characters to
     * represent any given byte.
     *
     * @param data a byte[] to convert to Hex characters
     * @return A String containing hexadecimal characters
     */
    public static String toHexString( byte[] data ) {

        int l = data.length;

        char[] out = new char[ l << 1 ];

        // two characters form the hex value.
        for ( int i = 0, j = 0; i < l; i++ ) {
            out[ j++ ] = hexDigits[ ( 0xF0 & data[ i ] ) >>> 4 ];
            out[ j++ ] = hexDigits[ 0x0F & data[ i ] ];
        }

        return new String( out );
    }
    
    public static String toSHA1( byte[] input ) {
        try {
            MessageDigest md = MessageDigest.getInstance( "SHA-1" );
            synchronized( md ) { 
                return toHexString( md.digest( input ) );
            }
        } catch ( NoSuchAlgorithmException nsae ) {
            log.error( "SHA algorithm not supported: " + nsae );
            throw new RuntimeException( "SHA not supported, can't continue" );
        }
    }
    
    public static String toSHA1( String input ) {
        try {
            return toSHA1( input.getBytes( "UTF-8" ) );
        } catch ( UnsupportedEncodingException uee  ) {
            log.error( "UTF-8 not supported: " + uee );
            return null;
        }
    }

    /**
     * Fingerprint beautify. A ':' character is placed between every
     * hex representation of the string.  Thus, "AF574B" becomes "AF:57:4B".
     */
    public static String fingerprintBeautifyHex( String hex ) {
        StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < hex.length(); i = i + 2 ) {
            if ( i > 0 ) {
                buf.append( ':' );
            }
            buf.append( hex.substring( i, i + 2 ) );
        }
        return buf.toString();
    }
 
    /**
     * Read all bytes from an InputStream and return them as a byte array.
     * @param is InputStream to read from.
     * @return the byte array built by reading all the bytes from the
     *         InputStream.
     */
    public static byte[] inputStreamToBytes( InputStream is ) throws IOException {
        if ( is == null ) {
            return new byte[] {};
        }
        // notice: this implementation is approximately twice as fast as IOUtils.toByteArray (commons-io-1.0)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[ 4096 ];
        int n;
        while ( ( n = is.read( buffer ) ) != -1 ) {
            baos.write( buffer, 0, n );
        }
        return baos.toByteArray();
    }
 
    /** "Escape" CR, LF and binary as such for logging purpose. */
    public static String loggingEscape( byte[] input ) {
        StringBuilder buf = new StringBuilder();
        for ( byte b : input ) {
            if ( b == '\r' ) {
                buf.append( "[\\r]" );
            } else if ( b == '\n' ) {
                buf.append( "[\\n]\n" );
            } else if ( b < 32 || b > 127 ) {
                buf.append( "[0x" )
                   .append( Integer.toHexString( unsignedByteToInt( b ) ) )
                   .append( "]" );
            } else {
                buf.append( (char) b );  // works only with ASCII compatible 32..127 characters
            }
        }
        return buf.toString();
    }
 
    public static void initForUnitTesting() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel( Level.WARN );  // shut up debugging/info on unit testing output
        PatternLayout pa = new PatternLayout();
        pa.setConversionPattern( "%d{ISO8601} %-5p %c{1}:%L(%M) - %m%n" );
        ( (Appender) Logger.getRootLogger().getAllAppenders().nextElement() ).setLayout( pa );
    }
 
    public static List<Integer> parseIntList( String input ) {
        List<Integer> retval = new ArrayList<Integer>();
        for ( String elem : input.split( "," ) ) {
            retval.add( Integer.parseInt( elem ) );
        }
        return retval;
    }

    public static Integer max( int... values ) {
        Integer retval = null; 
        for ( int value : values ) {
            if ( retval == null || value > retval ) {
                retval = value;
            }
        }
        return retval;
    }

    public static Double maxd( double... values ) {
        Double retval = null; 
        for ( double value : values ) {
            if ( retval == null || value > retval ) {
                retval = value;
            }
        }
        return retval;
    }
    
    public static Integer min( int... values ) {
        Integer retval = null; 
        for ( int value : values ) {
            if ( retval == null || value < retval ) {
                retval = value;
            }
        }
        return retval;
    }

    public static Double mind( double... values ) {
        Double retval = null; 
        for ( double value : values ) {
            if ( retval == null || value < retval ) {
                retval = value;
            }
        }
        return retval;
    }
    
    public static double time( Runnable runnable, int runs ) {
        List<Long> timings = new ArrayList<Long>();
        for ( int i = 0; i < runs; i++ ) {
            long now = System.currentTimeMillis();
            runnable.run();
            timings.add( System.currentTimeMillis() - now );
        }
        Collections.sort( timings );
        for ( int i = 0; i < runs/2; i++ ) {
            timings.remove( timings.size() - 1 );
        }
        int total = 0;
        for ( long timing : timings ) {
            total += timing;
        }
        return (double)total / timings.size();
    }
    
    public static boolean between( int v1, int v2, int v ) {
        if ( v1 > v2 ) {
            return v >= v2 && v <= v1;
        } else {
            return v >= v1 && v <= v2;
        }
    }

    private static double normalizeAngle( double angle ) {
        angle = angle % ( 2 * Math.PI );
        if ( angle > Math.PI ) {
            return angle - 2 * Math.PI;
        } else if ( angle <= -Math.PI ) {
            return angle + 2 * Math.PI;
        } else {
            return angle;
        }
    }
    
    public static double angleAdd( double angle1, double angle2 ) {
        return normalizeAngle( angle1 + angle2 );
    }
    
    public static double angleDiff( double angle1, double angle2 ) {
        return normalizeAngle( angle1 - angle2 );
    }

    public static int sqr( int value ) {
        return value * value;
    }

    public static double sqr( double value ) {
        return value * value;
    }

}