package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@Slf4j("logger")
class MetadataController {
    @Autowired
    ServiceRegistry services

    @GetMapping(path="/metadata", produces = "application/json")
    ResponseEntity getMetadata(@RequestParam String id) {
        LappsgridService service = services.getService(id)
        if (service == null) {
            return ResponseEntity.status(404).body("No service with ID " + id)
        }

        String json = null

        try {
            ServiceClient client = new ServiceClient(service.url, "tester", "tester")
            json = client.getMetadata()
        }
        catch (Exception e) {
            return ResponseEntity.status(500, "There was a problem retrieving the metadata. " + e.getMessage())
        }
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(json)
    }
}
