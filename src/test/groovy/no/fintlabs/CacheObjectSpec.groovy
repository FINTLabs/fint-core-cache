package no.fintlabs

import no.fintlabs.cache.CacheObjectFactory
import no.fintlabs.cache.packing.PackingTypes
import spock.lang.Specification

class CacheObjectSpec extends Specification {

    def "test equals for pojo cache object"() {
        given:
        def factory = new CacheObjectFactory<String>(PackingTypes.POJO)

        when:
        def cacheObject1 = factory.createCacheObject("The quick brown fox jumps over the lazy dog.", new int[]{})
        def cacheObject2 = factory.createCacheObject("The quick brown fox jumps over the lazy dog.", new int[]{})

        then:
        cacheObject1.equals(cacheObject2)
    }

    def "test not equals for pojo cache object"() {
        given:
        def factory = new CacheObjectFactory<String>(PackingTypes.POJO)

        when:
        def cacheObject1 = factory.createCacheObject("The quick brown fox jumps over the lazy dog.", new int[]{})
        def cacheObject2 = factory.createCacheObject("The quick brown fox jumps over the lazy dog!!!", new int[]{})

        then:
        !cacheObject1.equals(cacheObject2)
    }

    def "test equals for SERIALIZED cache object"() {
        given:
        def factory = new CacheObjectFactory<String>(PackingTypes.SERIALIZATION)

        when:
        def cacheObject1 = factory.createCacheObject("The quick brown fox jumps over the lazy dog.", new int[]{})
        def cacheObject2 = factory.createCacheObject("The quick brown fox jumps over the lazy dog.", new int[]{})

        then:
        cacheObject1.equals(cacheObject2)
    }

    def "test not equals for SERIALIZED cache object"() {
        given:
        def factory = new CacheObjectFactory<String>(PackingTypes.SERIALIZATION)

        when:
        def cacheObject1 = factory.createCacheObject("The quick brown fox jumps over the lazy dog.", new int[]{})
        def cacheObject2 = factory.createCacheObject("The quick brown fox jumps over the lazy dog!!!.", new int[]{})

        then:
        !cacheObject1.equals(cacheObject2)
    }
}
