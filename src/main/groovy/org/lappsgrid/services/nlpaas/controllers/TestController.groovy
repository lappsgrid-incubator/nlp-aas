package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.util.HTML
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@Slf4j("logger")
class TestController {

    @GetMapping(path="/test", produces = 'text/html')
    String test() {
        int x = 123
        return HTML.render('Test') {
            h1 'This is a test.'
            p id:'info', class:'hide', 'This is a hidden message.'
            p id:'message', 'This is a visible message'

            button(class:'button', type:'button', onclick:"test($x)", 'Click me')
        }
    }

    @GetMapping(path="/ajax/{id}", produces = 'text/html')
    String ajax(@PathVariable String id) {
        return HTML.render('AJAX') {
            h1 'AJAX Test'
            p 'Click the button.'
            p id:'message', class:'hide', 'Message goes here.'
            button type:'button', class:'button', onclick:"test_ajax('$id')", 'Click Me'
        }
    }

    @GetMapping(path="/test/ajax/{id}")
    ResponseEntity<String> testAjaxId(@PathVariable String id) {
        if (id.length()%2 == 0) {
            return ResponseEntity.ok("OK, that was cool.")
        }
        return ResponseEntity.status(404).body("Sorry bro-dude, you blew it.")
    }
}
