package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.services.nlpaas.dto.JobRequest
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.util.FormatConverters
import org.lappsgrid.services.nlpaas.work.ManagerService
import org.lappsgrid.services.nlpaas.work.WorkOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@RequestMapping("/submit")
@Slf4j("logger")
class SubmitController {
    @Autowired
    private ManagerService manager
    @Autowired
    private ServiceRegistry registry

    private FormatConverters converters = new FormatConverters()

    @PostMapping(consumes = "application/json")
    ResponseEntity submit(@RequestBody String body) {
        JobRequest request = Serializer.parse(body, JobRequest)
        String json
        String format = Discriminators.Uri.TEXT
        if (request.type == 'text/plain') {
//            Container container = new Container()
//            container.text = request.payload
            json = new Data(Discriminators.Uri.TEXT, request.payload).asJson()
        }
        else if (request.type == 'application/json') {
            Data data = new Data()
            data.discriminator = request.payload.discriminator
            data.payload = new Container((Map)request.payload.payload)
            json = data.asJson()
            format = data.discriminator
        }
        else {
            return ResponseEntity.status(415).body('Must be one of text/plain or application/json')
        }
        FormatConverters converters = new FormatConverters();
        WorkOrder order = new WorkOrder()
        for (String id : request.services) {
            LappsgridService service = registry.getService(id)
            if (service == null) {
                return ResponseEntity.status(400).body("Unknown service id $id".toString())
            }
            if (!service.requires.format.contains(format)) {
                String converter = converters.get(format, service.requires.format[0])
                if (converter) {
                    logger.debug("Adding converter {} to the pipeline.", converter)
                    order.services.add(registry.getService(converter))
//                    order.services.add(service)
                }
                else {
                    logger.debug("Unable to convert from {} to {}", format, service.requires.format.join(", "))
                    return ResponseEntity.status(400).body("Format mismatch. Unable to create pipeline.")
                }
            }
            order.services.add(service)
            format = service.produces.format[0]
        }
        order.json = json
        order.id = UUID.randomUUID().toString()
        manager.submit(order)
        return ResponseEntity.status(HttpStatus.CREATED)
                .header('Location', '/job/' + order.id)
                .build()
    }
}
