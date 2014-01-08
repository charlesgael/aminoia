package org.gc.amino.util;

/** Helpful class when manipulating data from anonymous classes. */
public class DataHolder<T> {
    /** The holded data. */
    public T data;
    public String toString() {
        return "{DataHolder{" + ( data == null ? "(null)" : data.toString() ) + "}}";
    }
    public DataHolder() {}
    public DataHolder( T val ) {
        data = val;
    }
}
