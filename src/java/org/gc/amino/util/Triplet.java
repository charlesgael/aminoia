package org.gc.amino.util;

import java.io.Serializable;

public class Triplet<T, U, V> implements Serializable {
    public T first;

    public U second;

    public V third;

    public Triplet( T first, U second, V third ) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public void set( T first, U second, V third ) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return "{first=" + first + ",second=" + second + ",third=" + third + "}";
    }

    @Override
    public boolean equals( Object anotherObject ) {
        if ( ! ( anotherObject instanceof Triplet ) ) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        Triplet otherTriplet = (Triplet) anotherObject;
        return first.equals( otherTriplet.first ) && second.equals( otherTriplet.second ) && third.equals( otherTriplet.third );
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode() + third.hashCode();
    }
}