package org.lappsgrid.services.nlpaas

import groovy.util.slurpersupport.GPathResult
import org.junit.Test
import org.lappsgrid.services.nlpaas.util.HTML

import javax.xml.bind.annotation.XmlType

/**
 *
 */
class HtmlTest {

    static final String TITLE = 'The Language Applications Grid'

    @Test
    void renderEmptyPage() {
        validate HTML.render { }
    }

    @Test
    void renderEmtpyClosure() {
        validate { }
    }

    @Test
    void simpleRender() {
        String html = HTML.render('Greet') {
            p 'Hello world'
        }
        GPathResult page = validate html, 'Greet'
        assert 'Hello world' == page.body.div[1].p[0].text()
        assert 1 == page.body.p.size()
        assert 1 == page.body.div[1].p.size()
    }

    @Test
    void pageWithData() {
        Map map = [ values: [1,2,3,4,5], name: 'Foo Bar']
        GPathResult html = validate(map) {
            h1 "The List"
            ul {
                map.values.each {
                    li(it)
                }
            }
            p id:'name', "My name is ${map.name}"
        }
        assert 'The List' == html.body.div[1].h1.text()
        assert 5 == html.body.div[1].ul.li.size()
        assert 'My name is Foo Bar' == html.body.div[1].p[0].text()

    }

    GPathResult parse(String html) {
        return new XmlSlurper().parseText(html)
    }

    GPathResult validate(Closure body) {
        return validate(TITLE, body)
    }

    GPathResult validate(String title, Closure body) {
        String html = HTML.render(title, body)
        return validate(html, title)
    }

    GPathResult validate(Map args, Closure body) {
        String title = args.title ?: TITLE
        String html = HTML.render(args, body)
        return validate(html, title)
    }


    GPathResult validate(String html) {
        return validate(html, 'The Language Applications Grid')
    }

    GPathResult validate(String html, String title) {
        GPathResult page = parse(html)
        assert title == page.head.title.text()
        assert 'stylesheet' == page.head.link.@rel.text()
        assert '/nlpaas/style/main.css' == page.head.link.@href.text()
        GPathResult copy = page.body.p[-1]
        assert 2 == page.body.div.size()
        assert null != copy
        assert 'p' == copy.name()
        assert 'copyright' == copy.@class.text()
        assert copy.text().startsWith('Copyright')
        assert copy.text().contains('The Language Applications Grid')
        return page
    }
}
