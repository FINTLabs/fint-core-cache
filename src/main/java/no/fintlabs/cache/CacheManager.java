package no.fintlabs.cache;

import no.fintlabs.cache.packing.PackingTypes;
import org.springframework.stereotype.Service;
import java.io.Serializable;

@Service
public class CacheManager {
    public <T extends Serializable> FintCache<T> create(PackingTypes packingTypes) {
        return new FintCache<T>(new CacheObjectFactory(packingTypes));
    }
}
