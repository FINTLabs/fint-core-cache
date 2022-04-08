package no.fintlabs.cache;

import no.fintlabs.cache.packing.PackingTypes;

import java.io.Serializable;

public class CacheManager {
    public <T extends Serializable> FintCache<T> create(PackingTypes packingTypes) {
        return new FintCache<T>(new CacheObjectFactory(packingTypes));
    }
}
