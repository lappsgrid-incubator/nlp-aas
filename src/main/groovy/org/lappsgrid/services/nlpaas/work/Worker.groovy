package org.lappsgrid.services.nlpaas.work

import groovy.util.logging.Slf4j
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.services.nlpaas.dto.Status
import org.lappsgrid.services.nlpaas.model.LappsgridService

/**
 *
 */
@Slf4j("logger")
class Worker implements Runnable {
    private ManagerService manager
    private WorkOrder order
    private boolean running

    Worker(ManagerService manager, WorkOrder order) {
        this.manager = manager
        this.order = order
        this.running = false
    }

    void halt() { running = false }
    void run() {
        logger.info("Worker starting for order {}", order.id)
        running = true
        manager.started(order.id)
        manager.status(order.id, Status.IN_PROGRESS)
        String json
        if (order.json instanceof String) {
            logger.debug("Payload is a string")
            json = order.json
        }
        else {
            logger.debug("Payload is an object")
            json = Serializer.toJson(order.json)
        }

        try {
            for (LappsgridService service : order.services) {
                if (running == false) {
                    manager.status(order.id, Status.STOPPED)
                    return
                }
                logger.trace("Calling service {}", service.id)
                ServiceClient client = new ServiceClient(service.url, "tester", "tester")
                json = client.execute(json)
            }
            logger.debug("Finished processing order {}", order.id)
            order.json = json
            manager.finished(order)
        }
        catch (Exception e) {
            logger.warn("Error during processing", e)
            manager.status(order.id, Status.ERROR, e.message)
        }
        running = false
    }
}
