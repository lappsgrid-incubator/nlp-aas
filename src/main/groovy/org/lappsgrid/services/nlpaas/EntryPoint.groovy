package org.lappsgrid.services.nlpaas

import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.util.Static
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

/**
 *
 */
@SpringBootApplication
class EntryPoint {
    static void main(String[] args) {
        Static.init()
        SpringApplication.run(EntryPoint, args)
    }

}
