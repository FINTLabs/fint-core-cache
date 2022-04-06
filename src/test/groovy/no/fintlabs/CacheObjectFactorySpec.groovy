package no.fintlabs

import no.fintlabs.cache.CacheObjectFactory
import no.fintlabs.cache.cacheObjects.PojoCacheObject
import no.fintlabs.cache.cacheObjects.SerializedCacheObject
import no.fintlabs.cache.packing.CompressingPacker
import no.fintlabs.cache.packing.PackingTypes
import no.fintlabs.cache.packing.SerializationPacker
import spock.lang.Specification

class CacheObjectFactorySpec extends Specification {

    def "Test deflate packer"() {
        given:
        def frodo = new TestObject("Frodo Lommelun")
        CacheObjectFactory<TestObject> cacheObjectFactory = new CacheObjectFactory<>(PackingTypes.DEFLATE)

        when:
        def cacheObject = cacheObjectFactory.createCacheObject(frodo, new int[]{})

        then:
        cacheObject instanceof SerializedCacheObject
        cacheObject.packer instanceof CompressingPacker
        frodo == cacheObject.unboxObject()
    }

    def "Test serialization packer"() {
        given:
        def frodo = new TestObject("Bilbo Lommelun", 44)
        CacheObjectFactory<TestObject> cacheObjectFactory = new CacheObjectFactory<>(PackingTypes.SERIALIZATION)

        when:
        def cacheObject = cacheObjectFactory.createCacheObject(frodo, new int[]{})

        then:
        cacheObject instanceof SerializedCacheObject
        cacheObject.packer instanceof SerializationPacker
        frodo == cacheObject.unboxObject()
    }

    def "Test pojo packer"() {
        given:
        def frodo = new TestObject("Frodo Lommelun", 23)
        CacheObjectFactory<TestObject> cacheObjectFactory = new CacheObjectFactory<>(PackingTypes.POJO)

        when:
        def cacheObject = cacheObjectFactory.createCacheObject(frodo, new int[]{})

        then:
        cacheObject instanceof PojoCacheObject
        frodo == cacheObject.unboxObject()
    }
}
