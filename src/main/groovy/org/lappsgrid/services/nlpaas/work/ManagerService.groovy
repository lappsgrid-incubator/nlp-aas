package org.lappsgrid.services.nlpaas.work

import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.util.StatsProvider
import org.lappsgrid.services.nlpaas.util.Time
import org.lappsgrid.services.nlpaas.dto.JobDescription
import org.lappsgrid.services.nlpaas.dto.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 */
@Service
class ManagerService implements StatsProvider {
    // Jobs older than this will be unceremoniously killed.
    static final String STATS_KILLED = 'manager.killed'
    static final String STATS_JOBS = 'manager.jobs'
    static final String STATS_WORKERS = 'manager.workers'
    static final String STATS_COUNT = "manager.count"

    @Value("reaper.manager.delay")
    private static final int REAPER_DELAY_MINUTES

    @Autowired
    private StorageService storage

    @Autowired
    private ThreadManagerService executor

    private ScheduledExecutorService reaper;
    private Map<String,JobDescription> index
    private Map<String,Worker> workers
    private long killed
    private long count

    ManagerService() {
        index = new HashMap<>()
        workers = new HashMap<>()
        reaper = (ScheduledExecutorService) Executors.newScheduledThreadPool(1);
        reaper.scheduleAtFixedRate(new Reaper(), 1, 1, TimeUnit.MINUTES)
        killed = 0
        count = 0
    }

    Set<String> list() {
        return index.keySet()
    }

    Collection<JobDescription> jobs() {
        return index.values()
    }

    Map<String, Integer> stats(Map<String,Integer> stats) {
        stats.put(STATS_JOBS, index.size())
        stats.put(STATS_WORKERS, workers.size())
        stats.put(STATS_KILLED, killed)
//        storage.stats(stats)
        executor.stats(stats)
        return stats
    }
    JobDescription get(String id) {
        return index[id]
    }

    boolean submit(WorkOrder order) {
        Worker worker = new Worker(this, order)
        JobDescription description = new JobDescription()
        description.status = Status.IN_QUEUE
        description.submittedAt = Time.now()
        description.id = order.id
        index[order.id] = description
        workers[order.id] = worker
        executor.execute(worker)
        return true
    }

    boolean status(String id, Status status) {
        JobDescription description = index[id]
        if (description == null) {
            return false
        }
        description.status = status
        return true
    }

    boolean status(String id, Status status, String message) {
        JobDescription description = index[id]
        if (description == null) {
            return false
        }
        description.status = status
        description.message = message
        return true
    }

    boolean started(String id) {
        JobDescription description = index[id]
        if (description == null) {
            return false
        }
        description.startedAt = Time.now()
        description.status = Status.IN_PROGRESS
        return true
    }

    boolean stopped(String id) {
        JobDescription description = index[id]
        if (description == null) {
            return false
        }
        description.stoppedAt = Time.now()
        description.status = Status.STOPPED
        return true
    }

    boolean remove(String id) {
        Worker worker = workers[id]
        if (worker) {
            worker.halt()
            workers.remove(id)
        }
        if (index.containsKey(id)) {
            index.remove(id)
            return true
        }
        return false
    }
//    boolean finished(String id) {
//        JobDescription description = index[id]
//        if (description == null) {
//            return false
//        }
//        description.finishedAt = new Date()
//        description.status = Status.DONE
//        description.resultUrl = '/download/' + id
//        return true
//    }

    boolean finished(WorkOrder order) {
        workers.remove(order.id)
        JobDescription description = index[order.id]
        if (description == null) {
            return false
        }
        try {
            storage.add(order.id, order.json)
        }
        catch (IOException e) {
            description.status = Status.ERROR
            description.message = e.message
            description.stoppedAt = Time.now()
            return false
        }
        description.finishedAt = Time.now()
        description.status = Status.DONE
        description.resultUrl = '/nlpaas/download/' + order.id
        return true
    }

    boolean message(String id, String message) {
        JobDescription description = index[id]
        if (description == null) {
            return false
        }
        description.message = message
        return true
    }

    boolean eta(String id, ZonedDateTime eta) {
        JobDescription description = index[id]
        if (description == null) {
            return false
        }
        description.eta = eta
        return true
    }

    @Slf4j("logger")
    class Reaper implements Runnable {
        void run() {
            logger.debug("Running reaper task")
            ZonedDateTime now = Time.now()
            List remove = []
            index.each { key, description ->
                long t = Time.since(description.submittedAt, ChronoUnit.MINUTES)
                if (t > REAPER_DELAY_MINUTES) {
                    remove.add(key)
                }
            }

            remove.each { id ->
                // Check if the worker thread is still alive.
                Worker worker = workers[id]
                if (worker) {
                    logger.info("Halting worker for {}", id)
                    worker.halt()
                    workers.remove(id)
                }
                logger.info("Removing job for {}", id)
                index.remove(id)
                ++killed
            }
        }
    }
}
