package org.lappsgrid.services.nlpaas.util

/**
 *
 */
class Static {
    static void init() {
        Collection.metaClass.map = { delegate.collect(it) }
        Collection.metaClass.filter = { delegate.grep(it) }
        Collection.metaClass.reduce = { i,a -> delegate.inject(i,a) }
    }

    private Static() {}
}
