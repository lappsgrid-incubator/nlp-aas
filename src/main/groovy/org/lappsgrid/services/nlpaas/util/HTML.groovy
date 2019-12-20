package org.lappsgrid.services.nlpaas.util

import groovy.xml.MarkupBuilder
import org.codehaus.groovy.control.CompilerConfiguration

import java.time.LocalDate

/**
 *
 */
class HTML {

    static class Layout {
        static final String main = '''import org.lappsgrid.eager.service.Version
html {
    head {
        title _.title
        link rel:'stylesheet', href:'/nlpaas/style/main.css\'   
        script src:'/nlpaas/js/jquery.js', ''
        script src:'/nlpaas/js/main.js', '' 
    }
    body {
        div(class:'header') {
            h1 'The Language Applications Grid'
            h2 'Natural Language Processing As A Service'
            p(class:'copyright', "Version ${Version.version}")
        }
        div(class:'content') {
            _.content()
        }
        int year = java.time.LocalDate.now().getYear()
        char copy = 0xA9
        p class:'copyright', "Copyright $copy $year The Language Applications Grid."
        
    }
}
'''
    }

    static String render(Closure body) {

        render('The Language Applications Grid', body)
    }

    static String render(String title, Closure body) {
        render([title: title], body)
    }

    static String render(Map data, Closure body) {
        StringWriter writer = new StringWriter()
        MarkupBuilder html = new MarkupBuilder(writer)

        body.delegate = html

        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = DelegatingScript.class.name
        Map<String,Object> underscore = [
                content: body,
//                title: data.title ?: "Language Applications Grid",
//                data: data
        ]
        data.each { k,v ->
            underscore[k] = v
        }
        if (underscore.title == null) underscore.title = 'The Language Applications Grid'
        Binding binding = new Binding()
        binding.setVariable("_", underscore)
        GroovyShell shell = new GroovyShell(binding, config)
        DelegatingScript script = (DelegatingScript) shell.parse(Layout.main)
        script.setDelegate(html)
        script.run()
        return writer.toString()
    }
}
