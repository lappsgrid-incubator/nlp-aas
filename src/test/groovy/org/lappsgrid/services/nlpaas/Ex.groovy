package org.lappsgrid.services.nlpaas

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.control.CompilerConfiguration

/**
 *
 */
class Layout {
    static String main = '''
html {
    head {
        title _.title
    }
    body {
        h1 "Greetings"  
        p "Hi there my name is Bot"
        if (_.content) {
            p "Running content"
            _.content()
        }         
        
    }
}
'''
}

class Ex {
    void run() {
        generate {
            h2 "Generated Content"
            p "Goodbye cruel world, I am leaving you today."
        }
    }

    void generate(Closure cl) {

        StringWriter writer = new StringWriter()
        MarkupBuilder markup = new MarkupBuilder(writer)

        cl.delegate = markup

        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = DelegatingScript.class.name
        Binding binding = new Binding()
        binding.setVariable("_", [content: cl, title: 'Hello Title'])
//        binding.setVariable("_", cl)
        GroovyShell shell = new GroovyShell(binding, config)
        DelegatingScript script = (DelegatingScript)shell.parse(Layout.main)
        script.setDelegate(markup)
        script.run()
        println writer.toString()
    }

    static void main(String[] args) {
        new Ex().run()
    }
}
