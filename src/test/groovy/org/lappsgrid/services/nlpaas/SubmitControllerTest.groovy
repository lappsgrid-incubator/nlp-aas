package org.lappsgrid.services.nlpaas

import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.services.nlpaas.controllers.JobController
import org.lappsgrid.services.nlpaas.controllers.SubmitController
import org.lappsgrid.services.nlpaas.dto.JobDescription
import org.lappsgrid.services.nlpaas.dto.Status
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

import java.time.ZonedDateTime

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = [SubmitController,JobController])
class SubmitControllerTest {
    @Autowired
    MockMvc mvc

    @MockBean
    private ManagerService managerService
    @MockBean
    private StorageService storage
    @MockBean
    private ThreadManagerService executor

    @Autowired
    private ServiceRegistry services

    @Test
    void postTextMeansExecuteCalledOnce() {
        String json = load("/tokenize-json.json")
        String url
        mvc.perform(post("/submit").content(json).contentType('application/json'))
                .andExpect(status().is(201))
                .andExpect(header().exists('Location'))
                .andDo( { MvcResult result -> url = result.response.getHeader('Location')})
        assert url.startsWith('/job/')
        String sid = url.replace('/job/', '')
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
    void postMixedGate() {
        String json = load("/mixed-gate.json")
        String url
        mvc.perform(post("/submit").content(json).contentType('application/json'))
                .andExpect(status().is(201))
                .andExpect(header().exists('Location'))
                .andDo( { MvcResult result -> url = result.response.getHeader('Location')})
        assert url.startsWith('/job/')
        String sid = url.replace('/job/', '')
        JobDescription description = new JobDescription()
        description.id = sid
        description.status = Status.IN_PROGRESS
        description.submittedAt = ZonedDateTime.now()
        Mockito.when(managerService.get(sid)).thenReturn(description)
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andDo(print())
    }

    String load(String name) throws IOException {
        InputStream input = this.class.getResourceAsStream(name)
        if (input == null) {
            throw new FileNotFoundException("Unable to load resource " + name)
        }
        return input.text
    }


    @TestConfiguration
    static class Configuration {
        @Bean
        ServiceRegistry serviceRegistry() { return new ServiceRegistry() }
    }

}
