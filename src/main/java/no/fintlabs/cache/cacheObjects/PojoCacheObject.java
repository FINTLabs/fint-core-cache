package no.fintlabs.cache.cacheObjects;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class PojoCacheObject<T extends Serializable> extends CacheObject<T> {

    private final T value;

    public PojoCacheObject(T value, int[] hashCodes) {
        super(hashCodes);
        this.value = value;
    }

    @Override
    public T unboxObject() {
        return value;
    }

    @Override
    public int getSize() {
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PojoCacheObject other)) return false;
        return value.equals(other.unboxObject());
    }
}
