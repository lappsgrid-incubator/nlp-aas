package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.util.HTML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@RequestMapping("/services")
@Slf4j("logger")
class ServicesController {

    @Autowired
    ServiceRegistry services

    @GetMapping(produces= MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List> listJson() {
        return ResponseEntity.ok(services.list().sort())
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    ResponseEntity<String> listHtml() {
        String html = HTML.render('Services') {
            h1 'All Services'
            table {
                th 'ID'
                th 'Description'
                services.list().sort().each { s ->
                    tr {
                        td s.id
                        td s.name
                    }
                }
            }
        }
        return ResponseEntity.ok(html)
    }
}
