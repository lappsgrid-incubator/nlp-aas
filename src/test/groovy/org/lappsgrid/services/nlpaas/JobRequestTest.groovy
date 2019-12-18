package org.lappsgrid.services.nlpaas

import org.junit.Test
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.services.nlpaas.dto.JobRequest

/**
 *
 */
class JobRequestTest {

    @Test
    void serializeLifPayload() {
        InputStream input = this.class.getResourceAsStream("/tokenize-json.json")
        assert null != input

        JobRequest request = Serializer.parse(input.text, JobRequest)
        assert Discriminators.Uri.LIF == request.payload.discriminator
        Container container = new Container((Map)request.payload.payload)
        println container.text
    }
}
