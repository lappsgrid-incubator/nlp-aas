package org.lappsgrid.services.nlpaas

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.lappsgrid.services.nlpaas.model.AnnotationsService
import org.lappsgrid.vocabulary.Annotations

/**
 *
 */
class AnnotationsServiceTest {

    AnnotationsService annotations;

    @Before
    void setup() {
        annotations = new AnnotationsService()
    }

    @After
    void teardown() {
        annotations = null
    }

    @Test
    void testSize() {
        assert 32 == annotations.size()
    }

    @Test
    void countNames() {
        int count = 0
        Iterator<String> it = annotations.names()
        while (it.hasNext()) {
            ++count
            println it.next()
        }
        assert annotations.size() == count
    }
    @Test
    void countValues() {
        int count = 0
        Iterator<String> it = annotations.values()
        while (it.hasNext()) {
            ++count
            it.next()
        }
        assert annotations.size() == count
    }
    @Test
    void countStream() {
        assert annotations.size() == annotations.stream().count()
    }

    @Test
    void testTokens() {
        assert null != annotations.get("Token")
    }

    @Test
    void testCaseSensitivity() {
        Set<String> seen = new HashSet<>()
        assert null != Annotations.TOKEN
        seen.add(Annotations.TOKEN)
        ['token', 'Token', 'TOKEN'].each {
            String type = annotations.get(it)
            if (type == null) {
                println "Type for $it is null"
                fail "Type for $it is null"
            }
            seen.add(type)

        }
        assert 1 == seen.size()
    }
}
