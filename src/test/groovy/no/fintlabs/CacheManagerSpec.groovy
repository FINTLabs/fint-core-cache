package no.fintlabs

import no.fintlabs.cache.CacheManager
import no.fintlabs.cache.packing.PackingTypes
import spock.lang.Specification

class CacheManagerSpec extends Specification {
    def "Create with different packing types"(PackingTypes packingType) {
        given:
        def fintCacheConfig = new CacheManager()

        when:
        def cache = fintCacheConfig.create(packingType)

        then:
        cache.cacheObjectFactory.packingType == packingType

        where:
        packingType | _
        PackingTypes.DEFLATE | _
        PackingTypes.SERIALIZATION | _
        PackingTypes.POJO | _
    }
}
