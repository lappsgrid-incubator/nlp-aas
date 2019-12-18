package org.lappsgrid.services.nlpaas.dto

/**
 *
 */
class JobRequest {
    List<String> services
    String type
    Object payload

    JobRequest() {
        services = []
    }

    JobRequest service(String id) {
        services.add(id)
        return this
    }

    JobRequest type(String type) {
        this.type = type
        return this
    }

    JobRequest payload(Object json) {
        this.payload = json
        return this
    }
}
