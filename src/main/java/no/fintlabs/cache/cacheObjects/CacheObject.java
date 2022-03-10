package no.fintlabs.cache.cacheObjects;

import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class CacheObject<T extends Serializable> implements Serializable {

    private final long lastUpdated;

    private final int[] hashCodes;

    public CacheObject(int[] hashCodes) {
        lastUpdated = System.currentTimeMillis();
        this.hashCodes = hashCodes;
    }

    public abstract T unboxObject();

    public abstract int getSize();

    public abstract boolean equals(Object anotherObject);
}
