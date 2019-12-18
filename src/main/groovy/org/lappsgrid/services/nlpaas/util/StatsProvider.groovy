package org.lappsgrid.services.nlpaas.util

/**
 *
 */
trait StatsProvider {
    Map<String,Integer> stats() {
        return stats(new HashMap<String,Integer>())
    }

    abstract Map<String,Integer> stats(Map<String,Integer> stats)
}