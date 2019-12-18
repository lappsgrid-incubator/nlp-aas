package org.lappsgrid.services.nlpaas.work

import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.util.StatsProvider
import org.springframework.stereotype.Service

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

/**
 *
 */
@Service
@Slf4j("logger")
class ThreadManagerService implements StatsProvider {

    private ThreadPoolExecutor executor

    ThreadManagerService() {
        int n = Runtime.getRuntime().availableProcessors() - 2
        if (n < 1) n = 1
        executor = Executors.newFixedThreadPool(n)
    }

    void execute(Runnable task) {
        executor.execute(task)
    }

    Map<String,Integer> stats(Map<String,Integer> stats) {
        stats.put("executor.active", executor.activeCount)
        return stats
    }
}
