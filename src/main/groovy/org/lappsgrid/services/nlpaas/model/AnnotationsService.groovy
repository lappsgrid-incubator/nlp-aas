package org.lappsgrid.services.nlpaas.model

import groovy.util.logging.Slf4j
import org.lappsgrid.vocabulary.Annotations
import org.springframework.stereotype.Service

import java.lang.reflect.Field
import java.util.stream.Stream

/**
 *
 */
@Service
@Slf4j("logger")
class AnnotationsService {

    Map<String,String> index

    AnnotationsService() {
        index = [:]
        Annotations.fields.each { Field f ->
            logger.debug("Processsing field {}", f.name)
            String type = f.get()
            int slash = type.lastIndexOf('/')
            if (slash > 0) {
                String name = type.substring(slash + 1)
                index[name.toLowerCase()] = type
            }
            else {
                logger.warn("No slash found in {}", type)
            }
        }
        index["token#pos"] = "http://vocab.lappsgrid.org/Token#pos"
        index["token#lemma"] = "http://vocab.lappsgrid.org/Token#lemma"
    }

    int size() { return index.size() }
    Iterator<String> names() { return index.keySet().iterator() }
    Iterator<String> values() { return index.values().iterator() }
    Stream<String> stream() { return index.values().stream() }

    String get(String type) {
        return index[type.toLowerCase()]
    }

}
