package org.lappsgrid.services.nlpaas

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.services.nlpaas.controllers.JobController
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.work.ManagerService
import org.lappsgrid.services.nlpaas.work.StorageService
import org.lappsgrid.services.nlpaas.work.ThreadManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
/**
 *
 */
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = JobController)
class MainTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ThreadManagerService threadManagerService
    @MockBean
    private ManagerService managerService
    @MockBean
    private ServiceRegistry services
    @MockBean
    private StorageService storage

    @Test
    void pingTest() {
        mvc.perform(get("/ping/123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pong 123")))
    }

    @Test
    void pingShouldFailWithoutToken() {
        mvc.perform(get("/ping"))
            .andExpect(status().is(404))
//            .andExpect(status().reason("Required String parameter 'token' is not present"))
    }

    @Test
    void testTest() {
        mvc.perform(get("/test"))
                .andExpect(status().is(404))
                .andExpect(status().reason("For oh four"))
    }
}
