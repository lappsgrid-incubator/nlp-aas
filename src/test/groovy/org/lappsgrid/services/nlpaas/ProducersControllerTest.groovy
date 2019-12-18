package org.lappsgrid.services.nlpaas

import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.services.nlpaas.controllers.ProducersController
import org.lappsgrid.services.nlpaas.controllers.ValidateController
import org.lappsgrid.services.nlpaas.json.Serializer
import org.lappsgrid.services.nlpaas.model.AnnotationsService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.vocabulary.Annotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 */
@Import(ControllerTestConfiguration)
@RunWith(SpringRunner)
@WebMvcTest(controllers = ProducersController)
class ProducersControllerTest {

    @Autowired
    MockMvc mvc

    @Autowired
    AnnotationsService annotation
    @Autowired
    ServiceRegistry registry

    @Test
    void tokenProducers() {
        def response = mvc.perform(get("/producers").param("type","token"))
                .andReturn()
                .getResponse()
        assert response.status == HttpStatus.OK.value()
        String json = response.contentAsString
        println json
        ProducersController.Body body = Serializer.parse(json, ProducersController.Body)
        assert HttpStatus.OK.value() == body.status
        assert null != body.producers
        assert 0 < body.producers.size()
    }

    @Test
    void caseSensitiveTokenProducers() {
        def response = mvc.perform(get("/producers").param("type","TOKEN"))
                .andReturn()
                .getResponse()
        assert response.status == HttpStatus.OK.value()
        String json = response.contentAsString
        ProducersController.Body body = Serializer.parse(json, ProducersController.Body)
        assert HttpStatus.OK.value() == body.status
        assert null != body.producers
        assert 0 < body.producers.size()
    }
    @Test
    void invalidTypeReturns404() {
        def response = mvc.perform(get("/producers").param("type", "FooBar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.status').value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath('$.message').value("No such annotation type FooBar"))
                .andDo(print())

    }

    @Test
    void noProducersReturns404() {
        // Nothing produces the base Annotation type.
        def response = mvc.perform(get("/producers").param("type", "annotation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.status').value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath('$.message').value("No services produce annotation"))
                .andDo(print())

    }

    @TestConfiguration
    static class Configuration {
        @Bean
        AnnotationsService annotationsService() { return new AnnotationsService() }
        @Bean
        ServiceRegistry serviceRegistry() { return new ServiceRegistry() }
    }
}
