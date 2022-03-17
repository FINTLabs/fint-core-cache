package no.fintlabs

import no.fintlabs.cache.CacheObjectFactory
import no.fintlabs.cache.FintCache
import no.fintlabs.cache.packing.PackingTypes
import spock.lang.Specification

import java.util.function.Predicate
import java.util.stream.Collectors

class FintCacheSpec extends Specification {

    def cache

    void setup() {
        def factory = new CacheObjectFactory(PackingTypes.DEFLATE)
        cache = new FintCache<TestObject>(factory)
    }

    def "Construct new cache"() {
        expect:
        cache.size() == 0
        cache.empty()
    }

    def "Add element to cache"() {
        given:
        def testObject = new TestObject("Frodo Lommelun");

        when:
        cache.put("key", testObject, new int[]{})

        then:
        cache.size() == 1
        testObject.equals(cache.stream().findFirst().get())
    }

    def "Add multiple elements to cache"() {
        when:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})

        then:
        cache.size() == 3
    }

    def "Update an element"() {
        given:
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

    def "Preserve insertion-order"() {
        when:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})
        cache.put("key4", new TestObject("Arwen"), new int[]{})
        cache.put("key5", new TestObject("Gollum"), new int[]{})

        then:
        def values = cache.stream().collect(Collectors.toList());
        values.get(0).name == "Samvis Gamgod";
        values.get(1).name == "Gandalv";
        values.get(2).name == "Tom Bombadil";
        values.get(3).name == "Arwen";
        values.get(4).name == "Gollum";
    }

    def "Filter element by hashCode"() {
        given:
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
        when:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})
        def lastUpdate = cache.getLastUpdated()
        cache.put("key4", new TestObject("Arwen"), new int[]{})
        cache.put("key5", new TestObject("Gollum"), new int[]{})

        then:
        cache.streamSince(lastUpdate).count() == 2

        // before lock on getLastUpdated & put, we expreienced this test to failed every 3-5 execution
        // probably because getLastUpdated and put was executed in the same milli secound
        where:
        i << (1..10)
    }

    def "Filter element by slice"() {
        given:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})
        cache.put("key4", new TestObject("Arwen"), new int[]{})
        cache.put("key5", new TestObject("Gollum"), new int[]{})

        when:
        def slice = cache
                .streamSlice(2, 1)
                .collect(Collectors.toList());

        then:
        slice.size() == 1
        slice.get(0).name.equals("Tom Bombadil")
    }

    def "Filter element by since/slice"() {
        given:
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv"), new int[]{})
        def lastUpdate = cache.getLastUpdated()
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})
        cache.put("key4", new TestObject("Arwen"), new int[]{})
        cache.put("key5", new TestObject("Gollum"), new int[]{})

        when:
        def slice = cache
                .streamSliceSince(lastUpdate, 1, 10)
                .collect(Collectors.toList());

        then:
        slice.size() == 2
        slice.get(0).name.equals("Arwen")
        slice.get(1).name.equals("Gollum")
    }

    def "Filter element by predicate"() {
        given:
        def hashCode = 123456789
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv", 21), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{hashCode})
        cache.put("key4", new TestObject("Arwen"), new int[]{hashCode})
        cache.put("key5", new TestObject("Gollum"), new int[]{hashCode})
        cache.put("key6", new TestObject("Gandalv", 22), new int[]{hashCode})

        when:
        def rigthPerson = "Gandalv"

        def result = cache.getLastUpdatedByFilter(hashCode, resource -> Optional
                .ofNullable(resource)
                .map(TestObject::getName)
                .map(rigthPerson::equals)
                .orElse(false)
        )

        then:
        result.get().name.equals("Gandalv")
        result.get().id == 22
    }


    def "Check retention not expired"() {
        when:
        cache.setRetentionPeriodInMs(5000)
        cache.put("key", new TestObject("Frodo Lommelun"), new int[]{})
        cache.evictOldCacheObjects()

        then:
        cache.size() == 1
    }

    def "Check retention when expired"() {
        when:
        cache.setRetentionPeriodInMs(5)
        cache.put("frodo-key", new TestObject("Frodo Lommelun"), new int[]{})
        sleep(10)
        cache.evictOldCacheObjects()

        then:
        cache.size() == 0
    }

    def "Check retention for multiple objects"() {
        when:
        cache.setRetentionPeriodInMs(100)
        cache.put("key1", new TestObject("Samvis Gamgod"), new int[]{})
        cache.put("key2", new TestObject("Gandalv", 21), new int[]{})
        cache.put("key3", new TestObject("Tom Bombadil"), new int[]{})
        cache.put("key4", new TestObject("Arwen"), new int[]{})
        sleep(101)
        cache.put("key5", new TestObject("Gollum"), new int[]{})
        cache.put("key6", new TestObject("Gandalv", 22), new int[]{})

        cache.evictOldCacheObjects()

        then:
        cache.size() == 2
    }

    def "Check retention when re-delivered"() {
        when:
        cache.setRetentionPeriodInMs(50)
        cache.put("frodo-key", new TestObject("Frodo Lommelun"), new int[]{})
        sleep(51)
        cache.put("frodo-key", new TestObject("Frodo Lommelun"), new int[]{})
        cache.evictOldCacheObjects()

        then:
        cache.size() == 1
    }
}
