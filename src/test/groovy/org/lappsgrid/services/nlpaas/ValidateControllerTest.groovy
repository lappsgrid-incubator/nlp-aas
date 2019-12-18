package org.lappsgrid.services.nlpaas

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.services.nlpaas.controllers.ValidateController
import org.lappsgrid.services.nlpaas.dto.JobRequest
import org.lappsgrid.services.nlpaas.json.Serializer
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print


/**
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ValidateController)
class ValidateControllerTest {
    @Autowired
    MockMvc mvc

    @Autowired
    ServiceRegistry registry //= new ServiceRegistry()


    @Test
    void simpleTest() {
        JobRequest request = new JobRequest()
                .service("stanford.tokenizer")
                .type("text/plain")
                .payload("Hello world")
        String json = Serializer.toJson(request)
        mvc.perform(post("/validate").content(json).contentType('application/json'))
            .andExpect(status().is(200))
    }

    @Test
    void invalidServiceId() {
        JobRequest request = new JobRequest()
                .service("foo.bar")
                .type("text/plain")
                .payload("Hello world")
        String json = Serializer.toJson(request)
        mvc.perform(post("/validate").content(json).contentType('application/json'))
                .andExpect(status().is(400))
    }

    @Test
    void twoStepPipeline() {
        String tokenizerId = "stanford.tokenizer"
        String taggerId = "stanford.tagger"
        JobRequest request = new JobRequest()
                .service(tokenizerId)
                .service(taggerId)
                .type("text/plain")
                .payload("Hello world")
        String json = Serializer.toJson(request)
        mvc.perform(post("/validate").content(json).contentType('application/json'))
                .andExpect(status().is(200))
    }

    @Test
    void invalidTwoStepPipeline() {
        String tokenizerId = "stanford.tokenizer"
        String taggerId = "stanford.tagger"
        JobRequest request = new JobRequest()
                .service(taggerId)
                .service(tokenizerId)
                .type("text/plain")
                .payload("Hello world")
        String json = Serializer.toJson(request)
        mvc.perform(post("/validate").content(json).contentType('application/json'))
                .andExpect(status().is(400))
    }

    @Test
    void opennlpShortcutTest() {
        String splitter = "opennlp.splitter"
        String tokenizer = "opennlp.tokenizer"
        String tagger = "opennlp.tagger"

        Container container = new Container()
        container.text = 'Goodbye cruel world. I am leaving you today.'
        Data data = new DataContainer(container)
        JobRequest request = new JobRequest()
            .service(splitter)
            .service(tokenizer)
            .service(tagger)
            .type('application/json')
            .payload(data)
        String json = Serializer.toJson(request)
        mvc.perform(post("/validate").content(json).contentType('application/json'))
            .andExpect(status().is(200))
    }

    @Test
    void wrapTextFirst() {
        String splitter = "opennlp.splitter"
        String tokenizer = "opennlp.tokenizer"
        String tagger = "opennlp.tagger"

        Container container = new Container()
        container.text = 'Goodbye cruel world. I am leaving you today.'
        Data data = new Data(Discriminators.Uri.LIF, container)
        JobRequest request = new JobRequest()
//                .service('anc:wrap.lif_1.0.0')
                .service(splitter)
                .service(tokenizer)
                .service(tagger)
//                .type('text/plain')
//                .payload('Hello world.')
                .type('application/json')
                .payload(data)
        String json = Serializer.toJson(request)
        println json
        mvc.perform(post("/validate").content(json).contentType('application/json'))
                .andExpect(status().is(200))
    }
    @Test
    void opennlpTokenizerShouldFailWithoutSplitterFirst() {
        String id = 'opennlp.tokenizer'
        JobRequest request = new JobRequest()
            .service(id)
            .type('text/plain')
            .payload('Hello world')
        String json = Serializer.toJson(request)

        mvc.perform(post("/validate").contentType('application/json').content(json))
                .andExpect(status().is(400))
    }

    @Test
    void invalidInputFormat() {
        Container container = new Container()
        container.text = "Hello world."
        Data data = new Data(Discriminators.Uri.TCF, container)
        JobRequest request = new JobRequest()
            .service("gate.tokenizer")
            .service('gate.splitter')
            .type('application/json')
            .payload(data)

        String json = Serializer.toJson(request)
        ContainsMatcher containsDoesNotAccept = new ContainsMatcher('does not accept')
        mvc.perform(post("/validate").contentType('application/json').content(json))
            .andExpect(status().is(400))
            .andExpect(jsonPath('$.status').value(400))
            .andExpect(jsonPath('$.message').value(containsDoesNotAccept))
            .andDo(print())

    }

    @Test
    void formatConversion() {
        JobRequest request = new JobRequest()
                .service("stanford.tokenizer")
                .service("lif2gate")
                .service('gate.splitter')
                .type('text/plain')
                .payload('Hello world.')

        String json = Serializer.toJson(request)
        mvc.perform(post("/validate").contentType('application/json').content(json))
            .andExpect(status().isOk())
    }

    class ContainsMatcher extends BaseMatcher<String> {

        String match

        ContainsMatcher(String match) {
            this.match = match
        }

        @Override
        boolean matches(Object o) {
            return o.toString().contains(match)
        }

        @Override
        void describeTo(Description description) {
            description.appendText("Substring match")
        }
    }

    @TestConfiguration
    static class ValidateConfiguration {
        @Bean
        ServiceRegistry serviceRegistry() {
//            println "Creating service registry"
            return new ServiceRegistry()
        }
//        @Bean
//        public MockMvc mockMvc() {
//            println "Creating MockMvc"
//            return MockMvcBuilders.standaloneSetup(new ValidateController()).build()
//        }
    }
}
