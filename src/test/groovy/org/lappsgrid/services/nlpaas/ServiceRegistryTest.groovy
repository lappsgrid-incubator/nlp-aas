package org.lappsgrid.services.nlpaas

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceList
import org.lappsgrid.services.nlpaas.model.ServiceRegistry

import static org.lappsgrid.discriminator.Discriminators.*

/**
 *
 */
class ServiceRegistryTest {

    ServiceRegistry registry
    @Before
    void setup() {
        registry = new ServiceRegistry()
    }

    @After
    void teardown() {
        registry = null
    }

    @Test
    void test() {
        println registry.size()
        registry.print()
    }

    @Test
    void list() {
        List<LappsgridService> services = registry.getProducers(Uri.TOKEN)
        println services.size()
        services.each { s -> println("${s.id} ${s.name}") }
    }

    @Test
    void iteratorTest() {
        Map<String, ServiceList> annotations = new HashMap<>()
        for (LappsgridService service : registry) {
            service.produces.annotations.each { type ->
                ServiceList list = annotations.get(type)
                if (list == null) {
                    list = new ServiceList()
                    annotations.put(type, list)
                }
                list.add(service)
            }
        }
        annotations.sort().each { entry ->
            println entry.getKey()
            ServiceList list = entry.getValue()
            entry.getValue().each { LappsgridService s ->
                println "\t${s.id}"
            }
            println()
        }
    }

    @Test
    void getServiceTest() {
        String id = "anc:opennlp.cloud.lemmatizer_pipeline_1.0.0"
        LappsgridService service = registry.getService(id)
        assert null != service
        assert id == service.id
        println service.url
    }

    @Test
    void opennlpShortcuts() {
        ['tokenizer', 'splitter', 'maxent', 'tagger', 'lemmatizer', 'ner'].each {
            assert null != registry.getService("opennlp." + it)
        }
    }

    @Test
    void stanfordShortcuts() {
        ['tokenizer', 'tagger', 'splitter', 'ner'].each {
            assert null != registry.getService("stanford." + it)
        }
    }

    @Test
    void lingpipeShortcuts() {
        ['tokenizer', 'splitter', 'tagger'                                                                                                                                                                                                          , 'ner'].each {
            assert null != registry.getService("lingpipe." + it)
        }
    }
}
