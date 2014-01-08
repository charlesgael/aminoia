package org.gc.amino.engine.terrainmap;

public class PointD {
    public double x, y;
    public PointD( double x, double y ) { 
        this.x = x;
        this.y = y;
    }
    public void set( double x, double y ) { 
        this.x = x;
        this.y = y;
    } 
    public String toString() {
        return x + "x" + y;
    }
    public boolean equals( Object anotherObject ) {
        if ( ! ( anotherObject instanceof PointD ) ) {
            return false;
        }
        PointD other_podouble = (PointD)anotherObject;
        return x == other_podouble.x && y == other_podouble.y;
    }
    public int hashCode() {
        return (int) ( x + y * 37 );
    }
}
