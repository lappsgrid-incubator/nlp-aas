package org.lappsgrid.services.nlpaas

import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.services.nlpaas.dto.ServiceCall
import org.lappsgrid.services.nlpaas.dto.Workflow
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.util.Static

import static org.lappsgrid.discriminator.Discriminators.*

/**
 *
 */
@Ignore
class WorkflowTest {

    @BeforeClass
    static void init() {
        Static.init()
    }

    @Test
    void gost() {
        String id = "anc:gost_1.0.0-SNAPSHOT"
        ServiceRegistry registry = new ServiceRegistry()
        LappsgridService service = registry.getService(id)
//        service.requires.annotations.each { println it }

        ServiceCall call = new ServiceCall()
        call.serviceUrl = service.url
        Workflow flow = new Workflow()
        flow.text = "Goodbye cruel world."
        flow.services.add(call)

        println Serializer.toPrettyJson(flow)
    }

    @Test
    void tokens() {
        ServiceRegistry registry = new ServiceRegistry()
        List<LappsgridService> services = registry.getProducers(Uri.NE)
        services.grep{ it.id.contains('stanford') }.each { LappsgridService s ->
            println s.id
            s.requires.annotations.each { println "\t$it"}
        }
    }

    @Test
    void map_filter_reduce() {
        ArrayList l
        [1,2,3,4].map{it*2}.filter{it%4==0}.each{ println it }
        def sum = { a,b -> a+b }
        int total = [1,2,3,4].map{it*2}.filter{it%4==0}.reduce(0,{i,j->i+j})
        println total
    }
}
