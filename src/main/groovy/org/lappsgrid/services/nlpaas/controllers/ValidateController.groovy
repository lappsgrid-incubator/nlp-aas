package org.lappsgrid.services.nlpaas.controllers

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View
import org.lappsgrid.services.nlpaas.dto.JobRequest
import org.lappsgrid.services.nlpaas.json.Serializer
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.util.FormatConverters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@Slf4j("logger")
class ValidateController {

    static final String JSON = MediaType.APPLICATION_JSON_VALUE

    @Autowired
    ServiceRegistry registry

    @PostMapping(path="/validate") //(consumes = JSON, produces = JSON)
    ResponseEntity<Object> validate(@RequestBody String json) {
        logger.info("Validating job request.")
        JobRequest job = null
        try {
            job = Serializer.parse(json, JobRequest)
        }
        catch (Exception e) {
            return errorParsingRequest(e)
        }
        if (job.services.size() == 0) {
            return ok()
        }

//        if (job.services.size() == 1) {
//            String id = job.services[0]
//            LappsgridService service = registry.getService(id)
//            if (service == null) {
//                return invalidServiceId(id)
//            }
//            return ok()
//        }

        String format = Discriminators.Uri.TEXT
        Set<String> annotations = new HashSet()
        if (job.type == JSON) {
            try {
                if (job.payload?.discriminator == Discriminators.Uri.LIF) {
                    format = Discriminators.Uri.LIF
                    Container container = new Container(job.payload.payload)
                    container.views.each { View view ->
                        view.metadata.contains.each { type,info ->
                            annotations.add(type)
                        }
                    }
                }
                else {
                    format = job.payload.discriminator
                }
            }
            catch (Exception e) {
                return errorParsingLif(e)
            }
        }
        FormatConverters converters = new FormatConverters()
        List<String> errors = []
        Iterator<String> it = job.services.iterator()
        while (it.hasNext()) {
            String id = it.next()
            LappsgridService s = registry.getService(id)
            if (s == null) {
                return invalidServiceId(id)
            }
            if (s.requires.format.contains(format)) {
                format = s.produces.format[0]
            }
            else {
                // See if we have a converter for the input format
                boolean canConvert = false
                s.requires.format.each { String fmt ->
                    String converter = converters.get(format,fmt)
                    if (converter != null) {
                        canConvert = true
                    }
                }
                if (canConvert) {
                    format = s.produces.format[0]
                }
                else {
                    return responseEntity(Body.BadRequest("$id does not accept $format"))
                }

            }
            Iterator<String> sit = s.requires.annotations.iterator()
            while (sit.hasNext()) {
                String required = sit.next()
                if (!annotations.contains(required)) {
                    logger.warn("The required annotation $required is missing")
                    errors.add("${s.id} requires $required".toString())
                }
                else {
                    logger.trace("Found required annotaiton $required")
                }
            }
            annotations.addAll(s.produces.annotations)
        }
        logger.debug("Annotations produced by the pipeline")
        annotations.each { logger.debug(it) }

        if (errors.size() > 0) {
            return missingAnnotations(errors)
        }

        return ok()
    }

    ResponseEntity ok() {
        return ResponseEntity.ok(Body.ok())
    }

    ResponseEntity<Body> missingAnnotations(List<String> annotations) {
        return responseEntity(Body.missingAnnotations(annotations))
    }
    ResponseEntity<Body> errorParsingRequest(Exception e) {
        return responseEntity(Body.errorParsingRequest(e))
    }
    ResponseEntity<Body> errorParsingLif(Exception e) {
        return responseEntity(Body.errorParsingLif(e))
    }

    ResponseEntity<Body> invalidServiceId(String id) {
        return responseEntity(Body.invalidService(id))
    }

    ResponseEntity<Body> invalidInputFormat() {
        return responseEntity()
    }
    ResponseEntity<Body> responseEntity(Body body) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TupleConstructor
    static class Body {
        int status
        String message
        String reason

        static Body ok() {
            return new Body(200, "OK")
        }

        static Body errorParsingRequest(Exception e) {
            return BadRequest("Error parsing request body.", e.message)
        }

        static Body errorParsingLif(Exception e) {
            return BadRequest('Unable to parse a LIF Data object', e.message)
        }

        static Body missingAnnotations(List<String> annotations) {
            return BadRequest("Required annotations are missing", annotations.join("\n"))
        }

        static Body invalidService(String id) {
            return BadRequest("Invalid service id " + id)
        }

        static Body BadRequest(String message) {
            return new Body(400, message)
        }

        static Body BadRequest(String message, List<String> reasons) {
            return BadRequest(message, reasons.join("\n"))
        }

        static Body BadRequest(String message, String reason) {
            return new Body(400, message, reason)
        }
    }
}
