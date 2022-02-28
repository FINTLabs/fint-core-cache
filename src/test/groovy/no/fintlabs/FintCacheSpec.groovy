package no.fintlabs

import no.fintlabs.cache.FintCache
import spock.lang.Specification

class FintCacheSpec extends Specification {

    def "Construct new cache"() {
        when:
        def cache = new FintCache<String>()

        then:
        cache.size() == 0
        cache.empty()
    }

    def "Add element to cache"() {
        given:
        def cache = new FintCache<TestObject>()
        def testObject = new TestObject("Frodo Lommelun");

        when:
        cache.put("key", testObject, new int[]{})

        then:
        cache.size() == 1
        testObject.equals(cache.stream().findFirst().get())
    }

    def "Add multiple elements to cache"() {
        given:
        def cache = new FintCache<TestObject>()

        when:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})

        then:
        cache.size() == 3
    }

    def "Update an element"() {
        given:
        def cache = new FintCache<TestObject>()
        def ringBearer1 = new TestObject("Bilbo Lommelun");
        def ringBearer2 = new TestObject("Frodo Lommelun");

        when:
        cache.put("key", ringBearer1, new int[]{})
        cache.put("key", ringBearer2, new int[]{})

        then:
        cache.size() == 1
        ringBearer2.equals(cache.stream().findFirst().get())
    }

    def "Don't update if the element is unchanged"() {
        given:
        def cache = new FintCache<TestObject>()
        def testObject = new TestObject("Frodo Lommelun");

        when:
        cache.put("key", testObject, new int[]{})
        def firstChange = cache.lastUpdated
        sleep(10)
        cache.put("key", testObject, new int[]{})
        def lastChange = cache.lastUpdated

        then:
        cache.size() == 1
        firstChange == lastChange
    }

    def "Filter element by hashCode"() {
        given:
        def cache = new FintCache<TestObject>()
        def hashCode = 123456789

        when:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{hashCode})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})

        then:
        cache.streamByHashCode(hashCode).count() == 1
        cache.streamByHashCode(hashCode).findFirst().get() == new TestObject("Gandalv")
    }

    def "Filter element by since"() {
        given:
        def cache = new FintCache<TestObject>()

        when:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})
        def lastUpdate = cache.getLastUpdated()
        // todo can fail if we dont wait 1ms. Fix?
        sleep(1)
        cache.put("key4", new TestObject("Arwen"), new int[]{})
        cache.put("key5", new TestObject("Gollum"), new int[]{})

        then:
        cache.streamSince(lastUpdate).count() == 2
    }
}
