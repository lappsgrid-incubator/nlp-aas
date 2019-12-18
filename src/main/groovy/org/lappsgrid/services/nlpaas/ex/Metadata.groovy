package org.lappsgrid.services.nlpaas.ex

import org.lappsgrid.api.WebService
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

//import sun.plugin2.message.Serializer

/**
 *
 */
class Metadata {
    static final String serviceUrl = " http://vassar.lappsgrid.org/invoker/anc:opennlp.cloud.lemmatizer_pipeline_1.0.0"

    static void _main(String[] args) {
        WebService service = new ServiceClient(serviceUrl, "tester", "tester")
        String json = service.getMetadata()
        Data data = Serializer.parse(json)
        ServiceMetadata metadata = new ServiceMetadata(data.payload)
        println "Requires"
        metadata.requires.annotations.each { println it }
        println "Produces"
        metadata.produces.annotations.each { println it }


    }
}
