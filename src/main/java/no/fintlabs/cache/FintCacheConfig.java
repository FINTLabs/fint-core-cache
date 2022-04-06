package no.fintlabs.cache;

import lombok.Setter;
import no.fintlabs.cache.packing.PackingTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FintCacheConfig {

    @Setter
    @Value("${fint.cache.packing-type:DEFLATE}")
    private String cachePackingType;

    @Bean
    public CacheObjectFactory getCacheObjectFactory() {
        PackingTypes packingType = PackingTypes.valueOf(cachePackingType);
        return new CacheObjectFactory(packingType);
    }
}
