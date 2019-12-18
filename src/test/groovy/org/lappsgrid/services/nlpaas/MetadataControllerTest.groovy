package org.lappsgrid.services.nlpaas

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.lappsgrid.discriminator.Discriminator
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.services.nlpaas.controllers.MetadataController
import org.lappsgrid.services.nlpaas.json.Serializer
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.mock.web.MockHttpServletResponse
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
@RunWith(SpringRunner)
@WebMvcTest(controllers = MetadataController)
class MetadataControllerTest {
    @Autowired
    MockMvc mvc
    @Autowired
    ServiceRegistry services

    @Test
    void standfordTokenizer() {
        String id = "stanford.tokenizer"
        MockHttpServletResponse response = mvc.perform(get("/metadata").param('id',id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn()
                .getResponse()
//        assert "application/json" == response.contentType
        Data data = Serializer.parse(response.contentAsString, Data)
        assert Discriminators.Uri.META == data.discriminator
        println response.contentAsString
    }

    @Test
    void converters() {
        mvc.perform(get('/metadata').param('id', 'lif2gate'))
            .andExpect(status().isOk())
            .andDo(print())
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        ServiceRegistry serviceRegistry() { return new ServiceRegistry() }
    }
}
