package org.lappsgrid.services.nlpaas

import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.services.nlpaas.controllers.JobController

//import org.lappsgrid.serialization.Serializer

import org.lappsgrid.services.nlpaas.dto.JobDescription
import org.lappsgrid.services.nlpaas.dto.Status
import org.lappsgrid.services.nlpaas.json.Serializer
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.work.ManagerService
import org.lappsgrid.services.nlpaas.work.StorageService
import org.lappsgrid.services.nlpaas.work.ThreadManagerService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = JobController)
class JobControllerTest {
    @Autowired
    MockMvc mvc

    @MockBean
    private ThreadManagerService threadManagerService
    @MockBean
    private ManagerService managerService
    @MockBean
    private ServiceRegistry services
    @MockBean
    private StorageService storage

//    @Test
    void postTextMeansExecuteCalledOnce() {
        String splitterId = "anc:opennlp.cloud.splitter_1.0.0"
        String tokenizerId = "anc:opennlp.cloud.tokenizer_1.0.0"
        LappsgridService tokenizer = new LappsgridService(tokenizerId, "Tokenizer", "http://tokenizer")
        LappsgridService splitter = new LappsgridService(splitterId, "Splitter", "http://splitter")
        String json = load("/tokenize-text.json")
        Mockito.when(services.getService(tokenizerId)).thenReturn(tokenizer)
        Mockito.when(services.getService(splitterId)).thenReturn(splitter)
        String url
        mvc.perform(post("/submit").content(json).contentType('application/json'))
            .andExpect(status().is(201))
            .andExpect(header().exists('Location'))
            .andDo( { MvcResult result -> url = result.response.getHeader('Location')})
        assert url.startsWith('/submit/')
        String sid = url.replace('/submit/', '')
        JobDescription description = new JobDescription()
        description.id = sid
        description.status = Status.IN_PROGRESS
        description.submittedAt = ZonedDateTime.now()
        Mockito.when(managerService.get(sid)).thenReturn(description)
        mvc.perform(get(url))
            .andExpect(status().isOk())
            .andDo(print())
    }

    @Test
    void statusOfInvalidIdReturns404() {
        mvc.perform(get('/job/123'))
            .andExpect(status().isNotFound())
    }

    @Test
    void checkJobStatus() {
        JobDescription description = new JobDescription()
        description.id = '123'
        description.submittedAt = ZonedDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC))
        description.status = Status.IN_QUEUE
        description.message = 'Hello world'
        Mockito.when(managerService.get('123')).thenReturn(description)

        String json
        mvc.perform(get('/job/123'))
            .andExpect(status().isOk())
            .andDo({MvcResult r -> json = r.response.contentAsString})

        JobDescription result = Serializer.parse(json, JobDescription)
        assert description.id == result.id
        assert description.submittedAt == result.submittedAt
        assert Status.IN_QUEUE == result.status
        assert 'Hello world' == result.message
    }

    @Test
    void invalidJobDelete() {
        mvc.perform(delete('/job/123'))
            .andExpect(status().isNotFound())
    }

    @Test
    void testDelete() {
        Mockito.when(managerService.remove('123')).thenReturn(true)
        mvc.perform(delete('/job/123'))
            .andExpect(status().isOk())
    }

    String load(String name) throws IOException {
        InputStream input = this.class.getResourceAsStream(name)
        if (input == null) {
            throw new FileNotFoundException("Unable to load resource " + name)
        }
        return input.text
    }

    @TestConfiguration
    class ServiceRegistryConfiguration {
        @Bean
        ServiceRegistry serviceRegistry() {
            return new ServiceRegistry()
        }

    }
}
