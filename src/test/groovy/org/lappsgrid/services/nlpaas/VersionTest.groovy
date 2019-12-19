package org.lappsgrid.services.nlpaas

import org.junit.Test
import org.lappsgrid.services.nlpaas.model.Version

/**
 *
 */
class VersionTest {

    @Test
    void lessThan() {
        lt('1.0.0', '2.0.0')
        lt('1.0.0','1.1.0')
        lt('1.0.0', '1.0.1')
        lt('1.0.0', '1.0.1-SNAPSHOT')
        lt('1.0.0-SNAPSHOT', '1.0.0')
        lt('1.2.3-RC-1', '1.2.3-RC-2')
    }

    @Test
    void greaterThan() {
        gt('2.0.0', '1.0.0')
        gt('1.1.0','1.0.0')
        gt('1.0.1', '1.0.0')
        gt('1.0.0', '1.0.0-SNAPSHOT')
        gt('1.0.1-SNAPSHOT', '1.0.0')
        gt('1.2.3-RC-2', '1.2.3-RC-1')
    }

    @Test
    void testEqual() {
        eq('1.0.0', '1.0.0')
        eq('2.2.2-RC-2', '2.2.2-RC-2')
        assert new Version("1.0.0").equals(new Version("1.0.0"))
        assert !new Version("1.0.1").equals(new Version("1.0.0"))
        assert new Version("1.0.0") != new Version("1.0.1")
    }

    void lt(String v1, String v2) {
        assert new Version(v1) < new Version(v2)
    }
    void gt(String v1, String v2) {
        assert new Version(v1) > new Version(v2)
    }
    void eq(String v1, String v2) {
        assert new Version(v1) == new Version(v2)
    }

}
