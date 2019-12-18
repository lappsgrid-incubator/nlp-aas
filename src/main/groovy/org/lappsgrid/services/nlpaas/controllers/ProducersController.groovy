package org.lappsgrid.services.nlpaas.controllers

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.model.AnnotationsService
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.util.HTML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@Slf4j("logger")
class ProducersController {

    @Autowired
    ServiceRegistry registry
    @Autowired
    AnnotationsService annotations

//    ProducerController(ServiceRegistry registry, AnnotationsService annotations) {
//        this.registry = registry
//        this.annotations = annotations
//    }

    @GetMapping(path="/producers", produces = "application/json")
    ResponseEntity<Body> producersJson(@RequestParam String type) {
        String url = annotations.get(type)
        if (url == null) {
            return notFound(type)
        }
        List<LappsgridService> services = registry.getProducers(url)
        if (services == null || services.size() == 0) {
            return ResponseEntity.status(404).body(new Body(404, "No services produce " + type))
        }

        List<String> ids = services.collect { s -> s.id }
        return ResponseEntity.status(200).body(new Body(ids))
    }

    @GetMapping(path="/producers", produces = "text/html")
    ResponseEntity<String> producersHtml(@RequestParam String type) {
        String url = annotations.get(type)
        def notfound = {
            HTML.render('Not Found') {
                h1 '404 Not Found'
                p "There are no services that produce $url"
            }
        }
        if (url == null) {
            return ResponseEntity.status(404).body(notfound())
        }
        List<LappsgridService> services = registry.getProducers(url)
        if (services == null || services.size() == 0) {
            return ResponseEntity.status(404).body(notfound())
        }

        String html = HTML.render("Producers") {
            h1 'Producers'
            p "The following services produce $url annotations."
            table {
                tr {
                    th 'Service ID'
                    th 'Formats'
                }
                services.each { s ->
                    tr {
                        td s.id
                        td s.requires.format.collect{t -> t.replace('http://vocab.lappsgrid.org/ns/media/','')}.join(', ')
                    }
                }
            }
//            ul {
//                services.each { s ->
//                    li s.id
//                }
//            }
        }
        return ResponseEntity.status(200).body(html)
    }

    @GetMapping(path="/consumers", produces = "application/json")
    ResponseEntity<Body> consumers(@RequestParam String type) {
        String url = annotations.get(type)
        if (url == null) {
            return notFound(type)
        }
        List<LappsgridService> services = registry.getProducers(url)
        if (services == null || services.size() == 0) {
            return ResponseEntity.status(404).body(new Body(404, "No services produce " + type))
        }

        List<String> ids = services.collect { s -> s.id }
        return ResponseEntity.status(200).body(new Body(ids))
    }

    ResponseEntity<Body> notFound(String type) {
        return ResponseEntity.status(404).body(new Body(404, "No such annotation type " + type))
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class Body {
        int status
        String message
        List<String> producers

        Body() { }
        Body(int status) {
            this(status, null)
        }

        Body(int status, String message) {
            this.status = status
            this.message = message
            this.producers = []
        }

        Body(List<String> producers) {
            this.status = 200
            this.message = null
            this.producers = producers
        }
    }

}
