package org.gc.amino.util;

import java.io.Serializable;

public class Couple<T, U> implements Serializable {
    public T first;
    public U second;
    public Couple() {}
    public Couple( T first, U second ) { 
        this.first = first;
        this.second = second;
    }
    public void set( T first, U second ) {
        this.first = first;
        this.second = second;
    }
    public String toString() {
        return "{first=" + first + ",second=" + second + "}";
    }
    public boolean equals( Object anotherObject ) {
        if ( ! ( anotherObject instanceof Couple ) ) {
            return false;
        }
        Couple otherCouple = (Couple)anotherObject;
        return first.equals( otherCouple.first ) && second.equals( otherCouple.second );
    }
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }
}
