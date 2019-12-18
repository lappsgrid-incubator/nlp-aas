package org.lappsgrid.services.nlpaas.util

import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.Services

import static groovyx.net.http.HttpBuilder.configure;
import static org.lappsgrid.discriminator.Discriminators.*

/**
 *
 */
class Utils {

    static String getConverter(String inputFormat, String outputFormat) {
        if (inputFormat == Uri.LIF && outputFormat == Uri.GATE) {
            return "lif2gate"
        }
        if (inputFormat == Uri.GATE && outputFormat == Uri.LIF) {

        }
    }


    static final String host = "https://api.lappsgrid.org"
    static void _main(String[] args) {
        URL url = new URL("https://api.lappsgrid.org/services/vassar")
        HttpBuilder http = configure {
            request.uri = host //"https://api.lappsgrid.org/services/vassar"
        }
        boolean save = false
        Services info = new Services()
        http.get {
            request.uri.path = '/services/brandeis'
            request.accept = "application/json"
            response.success { FromServer resp, Object body ->
                save = true
                info.url = body.url
                info.totalCount = body.totalCount
                info.services = []
                body.elements.each { e ->
                    if (e.serviceType == 'SERVICE.TYPE.PROCESSOR') {
                        LappsgridService service = new LappsgridService(e.serviceId, e.serviceName, e.endpointUrl)
                        if (service.update()) {
                            info.services.add(service)
                        }
                        else {
                            println("Unable to update metadata for ${e.endpointUrl}")
                        }
                    }
                    else {
                        println("${e.serviceId} is not a processing service")
                    }
                }
            }
            response.failure { FromServer resp, Object body ->
                if (resp.statusCode == 404) {
                    println "URL was not found on the server."
                }
                else {
                    println "Unhandled error: ${resp.statusCode}: ${resp.message}"
                }
            }
        }
        if (save) {
            File file = new File("/tmp/brandeis-services.json")
            file.text = Serializer.toPrettyJson(info)
            println "Wrote ${file.path}"
        }
        println "Done"
    }
}
