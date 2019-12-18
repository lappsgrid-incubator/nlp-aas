package org.lappsgrid.services.nlpaas.work

import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.util.StatsProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 */
@Service
@Slf4j("logger")
class StorageService implements StatsProvider {

    private static String STATS_SIZE = "storage.files"
    private static String STATS_DELETED = "storage.deleted"

    // Times are in minutes unless otherwise stated.
    // Files older than this will be deleted
    @Value("time.file.age")
    private static final int FILE_AGE_LIMIT = 30

    // How often to run the reaper task.
    @Value("time.file.delay")
    private static final int REAPER_TIME_DELAY = 5

    private Map<String,Path> index
    private long deleted

    @Value("storage.directory")
    private String directoryPath
    private Path directory

    private ScheduledExecutorService executor;

    StorageService() {
        deleted = 0
        index = new HashMap<>()
        if (directoryPath == null) {
            directoryPath = "/tmp/nlpaas"
        }
        executor = (ScheduledExecutorService) Executors.newScheduledThreadPool(1)
        executor.scheduleAtFixedRate(new Reaper(), REAPER_TIME_DELAY, REAPER_TIME_DELAY, TimeUnit.MINUTES)
        directory = Paths.get(directoryPath)
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory)
            }
            catch (IOException e) {
                logger.error("Unable to create storage directory.", e)
                directory = null
            }
        }
    }

    void add(String id, String json) throws IOException {
        if (directory == null) {
            return
        }

        String filename = id + ".json"
        Path output = directory.resolve(filename)
        if (json.length() < 16384) {
            Files.write(output, json.bytes)
            index[id] = output
        }
        else {
            BufferedWriter writer = Files.newBufferedWriter(output)
            writer.write(json, 0, json.length())
            writer.flush()
            writer.close()
            index[id] = output
        }
        logger.info("Wrote {} to {}", id, output.toString())
    }

    String get(String id) {
        if (directory == null) {
            return null
        }
        Path path = index[id]
        if (path == null) {
            logger.warn("Document not found {}", id)
            return null
        }
        if (!Files.exists(path)) {
            logger.warn("Document was in index but is now gone: {}", id)
            index.remove(id)
            return null
        }
        return new String(Files.readAllBytes(path))

    }

    void remove(String id) {
        Path path = index[id]
        if (path == null) {
            logger.warn("Attempted to remove non-existent file {}", id)
            return
        }
        index.remove(id)
        try {
            Files.delete(path)
            logger.info("Deleted file {}", path.toString())
        }
        catch (IOException e) {
            logger.error("Unable to delete {}", path.toString(), e)
        }

    }

    boolean exists(String id) {
        return index[id] != null
    }

    @Override
    Map<String, Integer> stats(Map<String, Integer> stats) {
        stats.put(STATS_SIZE, index.size())
        stats.put(STATS_DELETED, deleted)
        return stats
    }

    @Slf4j("logger")
    class Reaper implements Runnable {
        void run() {
            logger.debug("Running the storage system reaper.")
            Instant cutoff = Instant.now().minus(FILE_AGE_LIMIT, ChronoUnit.MINUTES)
            List remove = []
            index.each { id, path ->
                BasicFileAttributes atts = Files.readAttributes(path, BasicFileAttributes)
                FileTime t = atts.creationTime()
                if (t.toInstant().isBefore(cutoff)) {
                    remove.add(id)
                    logger.info("Removing stale file for {}", id)
                    if (Files.exists(path)) try {
                        Files.delete(path)
                        ++deleted
                    }
                    catch (IOException e) {
                        logger.error("Error deleting file {}", path.toString(), e)
                    }
                }
            }
            remove.each { index.remove(it) }
        }
    }

    @Slf4j("logger")
    class DeleteFiles implements Runnable {
        void run() {
            logger.info("Running the file system reaper")
        }
    }
}
