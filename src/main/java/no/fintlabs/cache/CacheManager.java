package no.fintlabs.cache;

import no.fintlabs.cache.packing.PackingTypes;

@SuppressWarnings(value = "uncheched")
public class CacheManager {
    public FintCache create(PackingTypes packingTypes) {
        return new FintCache(new CacheObjectFactory(packingTypes));
    }
}
