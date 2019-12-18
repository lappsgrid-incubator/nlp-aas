package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.services.nlpaas.util.HTML
import org.lappsgrid.services.nlpaas.work.ManagerService
import org.lappsgrid.services.nlpaas.work.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@Slf4j("logger")
@RequestMapping("/stats")
class StatsController {
    @Autowired
    private ManagerService manager
    @Autowired
    private StorageService storage

    @GetMapping(produces = "appication/json")
    ResponseEntity stats() {
        Map stats = manager.stats()
        storage.stats(stats)
        return ResponseEntity.ok(Serializer.toPrettyJson(stats))
    }

    @GetMapping(produces = "text/html")
    String getHtmlStats() {
        return HTML.render {
            h1 "Manager"
            table {
                tr {
                    th 'Name'
                    th 'Value'
                }
                manager.stats().sort().each { name,value ->
                    tr {
                        td name
                        td value
                    }
                }
            }
            h1 "Storage"
            table {
                tr {
                    th 'Name'
                    th 'Value'
                }
                storage.stats().sort().each { name,value ->
                    tr {
                        td name
                        td value
                    }
                }
            }
        }
    }


}
