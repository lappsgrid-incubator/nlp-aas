package org.lappsgrid.services.nlpaas.model

import groovy.util.logging.Slf4j
import org.lappsgrid.metadata.IOSpecification
import org.lappsgrid.serialization.Serializer
import org.springframework.stereotype.Service

import java.util.function.Consumer

/**
 *
 */
@Slf4j("logger")
@Service
class ServiceRegistry implements Iterable<LappsgridService> {
    Map<String,LappsgridService> index = new HashMap<>()
    Map<String, List<LappsgridService>> producers = new HashMap<>()
    Map<String, List<LappsgridService>> consumers = new HashMap<>()
    Map converterIndex = [:]

    public ServiceRegistry() {
        load('/vassar.json')
        load('/brandeis.json')
        addShortcuts()
//        for (LappsgridService converter : getFormatConverters()) {
//            for (String inputFormat : converter.requires.format) {
//                for (String outputFormat : converter.produces.format) {
//                    addConverter(inputFormat, outputFormat, converter)
//                }
//            }
//        }
    }

    int size() {
        return index.size()
    }

    void add(LappsgridService service, IOSpecification spec, Map map) {
        spec.annotations.each { type ->
            List<LappsgridService> services = map[type]
            if (services == null) {
                services = []
                map[type] = services
            }
            services.add(service)
        }
    }

    /*
    List<LappsgridService> getConverters(String inputFormat, String outputFormat) {
        Map outputs = converterIndex[inputFormat]
        if (outputs == null) {
            return []
        }
        return outputs[outputFormat]
    }

    private void addConverter(String input, String output, LappsgridService service) {
        Map<String,String> outputs = converterIndex[input]
        if (outputs == null) {
            outputs = [:]
            converterIndex[input] = outputs
        }
        List<LappsgridService> services = outputs[output]
        if (services == null) {
            services = []
            outputs[output] = services
        }
        services.add(service)
    }
    */
    Collection<LappsgridService> getFormatConverters() {
        return  index.values().findAll{ s -> s.isConverter() }
    }

    void load(String name) {
        InputStream stream = this.class.getResourceAsStream(name)
        if (stream == null) {
            logger.warn("Unable to load data for {}", name)
            return
        }
        Services services = Serializer.parse(stream.text, Services)
        services.services.each { LappsgridService service ->
            index.put(service.id, service)
            add(service, service.requires, consumers)
            add(service, service.produces, producers)
        }
    }

    private void addShortcuts() {
        // All the other tools use "tagger" so we will add a shortcut for the
        // OpenNLP MaxEnt tagger.
        LappsgridService tagger = index["anc:opennlp.cloud.maxent_1.0.0"]
        if (tagger) {
            index["anc:opennlp.cloud.tagger_1.0.0"] = tagger
            index["opennlp.tagger"] = tagger
        }
        else {
            logger.error("The OpenNLP maxent tagger is missing.")
        }

        ['tokenizer', 'splitter', 'maxent', 'lemmatizer', 'ner'].each { type ->
            opennlp(type)
        }
        ['tokenizer', 'tagger', 'splitter', 'ner'].each { type ->
            stanford(type)
        }
        ['tokenizer', 'splitter', 'tagger', 'ner'].each { type ->
            lingpipe(type)
        }
        ['tokenizer', 'splitter', 'tagger', 'npchunker', 'vpchunker', 'ortho', 'ner', 'gazetteer'].each { type ->
            gate(type)
        }
        addShortcut("opennlp.ner.pipeline", "anc:opennlp.cloud.ner_pipeline_1.0.0")
        addShortcut("stanford.nep.pipeline", "anc:stanford.cloud.ner_1.0.0")
        addShortcut("lif2gate", "anc:convert.json2gate_2.0.0")
        addShortcut("gate2lif", "anc:convert.gate2json_2.0.0")
    }

    private void stanford(String name) {
        String id = "anc:stanford.${name}_2.1.0-SNAPSHOT"
        String shorcut = "stanford.$name"
        addShortcut(shorcut, id)
    }

    private void gate(String name) {
        String id = "anc:gate.${name}_2.3.0"
        String shortcut = "gate.$name"
        addShortcut(shortcut, id)
    }

    private String opennlp(String name) {
        String id = "anc:opennlp.cloud.${name}_1.0.0"
        String shortcut = "opennlp." + name
        addShortcut(shortcut, id)
    }

    private String lingpipe(String name) {
        String id = "anc:lingpipe.${name}_1.1.1-SNAPSHOT"
        String shortcut = "lingpipe." + name
        addShortcut(shortcut, id)
    }

    private void addShortcut(String shortcut, String id) {
        LappsgridService service = index[id]
        if (service) {
            index[shortcut] = service
        }
        else {
            logger.error("Unknown service $id")
        }
    }

    LappsgridService getService(String id) {
        return index.get(id)
    }

    List<LappsgridService> getProducers(String type) {
        producers.get(type)
    }

    List<LappsgridService> getConsumers(String type) {
        consumers.get(type)
    }

    Collection<LappsgridService> list() {
        return index.values()
    }

    void print() {
        index.each { n,s -> println "$n $s.name"}
    }

    @Override
    Iterator<LappsgridService> iterator() {
        return new ServiceIterator()
    }

    @Override
    void forEach(Consumer<? super LappsgridService> action) {
        index.forEach(action)
    }

//    @Override
//    Spliterator<LappsgridService> spliterator() {
//        return super.spliterator()
//    }

    class ServiceIterator implements Iterator<LappsgridService> {
        Iterator<Map.Entry> it = index.iterator()

        @Override
        boolean hasNext() {
            return it.hasNext()
        }

        @Override
        LappsgridService next() {
            Map.Entry<String,LappsgridService> entry = it.next()
            return (LappsgridService) entry.getValue()
        }
    }
}
