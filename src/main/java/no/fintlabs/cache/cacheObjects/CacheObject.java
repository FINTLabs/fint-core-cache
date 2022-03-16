package no.fintlabs.cache.cacheObjects;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
public abstract class CacheObject<T extends Serializable> implements Serializable {

    private final long lastUpdated;

    private long lastDelivered;

    private final int[] hashCodes;

    public CacheObject(int[] hashCodes) {
        lastUpdated = System.currentTimeMillis();
        lastDelivered = lastUpdated;
        this.hashCodes = hashCodes;
    }

    public abstract T unboxObject();

    public abstract int getSize();

    public abstract boolean equals(Object anotherObject);

    public void refreshLastDelivered() {
        lastDelivered = System.currentTimeMillis();
    }
}
