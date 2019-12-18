package org.lappsgrid.services.nlpaas

import org.lappsgrid.services.nlpaas.model.AnnotationsService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/**
 *
 */
//@TestConfiguration
class ControllerTestConfiguration {
//    @Bean
    public static ServiceRegistry serviceRegistry() {
        return new ServiceRegistry()
    }

//    @Bean
    static AnnotationsService annotationsService() {
        return new AnnotationsService()
    }
}
