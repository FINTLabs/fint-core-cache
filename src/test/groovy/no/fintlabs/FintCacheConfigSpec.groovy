package no.fintlabs

import no.fintlabs.cache.FintCacheConfig
import no.fintlabs.cache.packing.PackingTypes
import spock.lang.Specification

class FintCacheConfigSpec extends Specification {
    def "On given packingtype"(a, b) {
        given:
        def fintCacheConfig = new FintCacheConfig()

        when:
        fintCacheConfig.setCachePackingType(a)
        def cacheObjectFactory = fintCacheConfig.getCacheObjectFactory()

        then:
        cacheObjectFactory.packingType == b

        where:
        a               | b
        'DEFLATE'       | PackingTypes.DEFLATE
        'SERIALIZATION' | PackingTypes.SERIALIZATION
        'POJO'          | PackingTypes.POJO
    }
}
