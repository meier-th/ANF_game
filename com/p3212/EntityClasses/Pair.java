package com.p3212.EntityClasses;

import java.io.Serializable;
import java.util.Objects;

public class Pair<K, V> implements Serializable {
    
    private final K key;
    
    private final V value;
    
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    } 

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.key);
        hash = 79 * hash + Objects.hashCode(this.value);
        return hash;
    }
    
    @Override
    public String toString() {
        return this.key.toString()+"="+this.value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Pair))
            return false;
        return ((Pair<K, V>)obj).key.equals(this.key) && ((Pair<K, V>)obj).value.equals(this.value);
    }
    
    
    
}
