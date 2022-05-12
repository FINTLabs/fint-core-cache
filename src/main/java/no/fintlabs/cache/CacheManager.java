package no.fintlabs.cache;

import no.fintlabs.cache.packing.PackingTypes;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CacheManager {

    private final List<FintCache> caches = new ArrayList<>();

    public <T extends Serializable> FintCache<T> create(PackingTypes packingTypes, String orgId, String model) {
        FintCache<T> newCache = new FintCache<T>(new CacheObjectFactory(packingTypes), CacheUri.create(orgId, model));
        caches.add(newCache);
        return newCache;
    }

    public Stream<FintCache> getCaches() {
        return caches.stream();
    }

    public void removeAll() {
        caches.clear();
    }

    public void remove(FintCache cacheToRemove) {
        caches.remove(cacheToRemove);
    }
}
