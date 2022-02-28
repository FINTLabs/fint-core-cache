package no.fintlabs

import no.fintlabs.cache.FintCache
import spock.lang.Specification

class CacheSpec extends Specification {

    def "Construct new cache"() {
        given:

        when:
        def cache = new FintCache<String>()

        then:
        cache.size() == 0
    }
}
