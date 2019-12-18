package org.lappsgrid.services.nlpaas.util

import static org.lappsgrid.discriminator.Discriminators.*

/**
 *
 */
class FormatConverters {
    Map<String, Map<String,String>> inputs = [:]

    FormatConverters() {
        add(Uri.LIF, Uri.GATE, 'lif2gate')
        add(Uri.GATE, Uri.LIF, 'gate2lif')
        add(Uri.TCF, Uri.LIF, 'anc:tcf-converter_1.0.1')
        add(Uri.LIF, Uri.TCF, 'anc:weblicht.lif.converter_1.0.0')
    }

    String get(String inputFormat, String outputFormat) {
        Map outputs = inputs[inputFormat]
        if (outputs == null) {
            return null
        }
        return outputs[outputFormat]
    }

    void add(String input, String output, String serviceId) {
        Map<String,String> outputs = inputs[input]
        if (outputs == null) {
            outputs = [:]
            inputs[input] = outputs
        }
        outputs[output] = serviceId
    }
}
