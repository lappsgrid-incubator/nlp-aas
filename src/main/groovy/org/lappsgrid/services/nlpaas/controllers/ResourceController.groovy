package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@Slf4j("logger")
//@RestController
class ResourceController {


    @GetMapping("/style/main.css")
    String getStyleSheet() {
        InputStream input = this.class.getResourceAsStream("/style/main.css")
        return input.text
    }

    @GetMapping("/js/jquery.js")
    String getJQuery() {
        InputStream input = this.class.getResourceAsStream("/js/jquery-3.4.1.min.js")
        return input.text
    }

    @GetMapping("/js/main.js")
    String getMainJs() {
        InputStream input = this.class.getResourceAsStream("/js/main.js")
        return input.text
    }
}
