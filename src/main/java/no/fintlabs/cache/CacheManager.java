package no.fintlabs.cache;

import no.fintlabs.cache.packing.PackingTypes;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CacheManager {

    private final List<Cache> caches = new ArrayList<>();

    public <T extends Serializable> Cache<T> create(PackingTypes packingTypes, String orgId, String model) {
        Cache<T> newCache = new FintCache<T>(new CacheObjectFactory(packingTypes), CacheUri.create(orgId, model));
        caches.add(newCache);
        return newCache;
    }

    public Stream<Cache> getCaches() {
        return caches.stream();
    }

    public void removeAll() {
        caches.clear();
    }

    public void remove(Cache cacheToRemove) {
        caches.remove(cacheToRemove);
    }
}
