package org.lappsgrid.services.nlpaas

import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.services.nlpaas.dto.JobRequest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import static org.lappsgrid.discriminator.Discriminators.*


/**
 *
 */
class DataCreator {

    String id(String type) {
        return "anc:opennlp.cloud.${type}_1.0.0"
    }
    void text() {
        JobRequest request = new JobRequest()
        [ 'splitter', 'tokenizer', 'maxent', 'lemmatizer' ].collect{ id(it) }.each { request.service(it) }
        request.type = "text/plain"
        request.payload = "Goodbye cruel world I am leaving you today."
        println Serializer.toPrettyJson(request)
    }

    void json() {
        InputStream stream = this.class.getResourceAsStream("/pubmed.lif")
        if (stream == null) {
            println "Unable to load pubmed file"
            return
        }
        JobRequest request = new JobRequest()
        [ 'tokenizer', 'splitter', 'maxent', 'lemmatizer' ].collect{ id(it) }.each { request.service(it) }
        request.type = "application/json"
        request.payload = Serializer.parse(stream.text)
        println Serializer.toPrettyJson(request)
    }

    void json2() {
        JobRequest request = new JobRequest()
        ['splitter', 'tokenizer'].collect{ id(it) }.each { request.services.add(it) }
        request.type('application/json')
        Container container = new Container()
        container.text = "Goodbye cruel world I am leaving you today."
        request.payload = new Data(Uri.LIF, container)
        println Serializer.toPrettyJson(request)
    }

    void print(ZonedDateTime time, DateTimeFormatter format) {
        String s = time.format(format);
        ZonedDateTime t = ZonedDateTime.parse(s, format)
        println s
        println t.format(format)
        println()
    }

    void timetests() {
//        ZoneId.availableZoneIds.sort()each { println it }
//        if (true) return

//        println "date      " + new Date()
//        println "local     " + LocalDateTime.now();
//        println "local est " + LocalDateTime.now(ZoneId.of("EST5EDT"))
//        println "local gmt " + LocalDateTime.now(ZoneId.of("GMT"))
//        println "local utc " + LocalDateTime.now(ZoneId.of("UTC"))
//        println "zoned     " + ZonedDateTime.now()
//        println "zoned est " + ZonedDateTime.now(ZoneId.of("EST5EDT"))
//        println "zoned gmt " + ZonedDateTime.now(ZoneId.of("GMT"))
//        println "zoned utc " + ZonedDateTime.now(ZoneId.of("UTC"))
//        println "zoned off " + ZonedDateTime.now(ZoneId.ofOffset("", ZoneOffset.ofHours(0)))
//        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"))

//        print(time, DateTimeFormatter.BASIC_ISO_DATE)
//        print(time, DateTimeFormatter.ISO_DATE)
//        print(time, DateTimeFormatter.ISO_DATE_TIME)
//        print(time, DateTimeFormatter.ISO_LOCAL_TIME)
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"))
        ZonedDateTime t1 = ZonedDateTime.parse("2019-12-02T18:32:53.534Z")
        println time
        println t1
        long diff = ChronoUnit.DAYS.between(t1, time)
        println diff
//        print(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
//        print(time, DateTimeFormatter.ISO_ZONED_DATE_TIME)
//        print(time, DateTimeFormatter.RFC_1123_DATE_TIME)


    }

    void path() {
        Path path = Paths.get("/tmp/powerlog")
        BasicFileAttributes atts = Files.readAttributes(path, BasicFileAttributes)
        FileTime t = atts.creationTime()
        Instant now = Instant.now().minus(10, ChronoUnit.MINUTES)
        println t
        println now
        if (t.toInstant().isBefore(now)) {
            println "File is before cutoff"
        }
        else {
            println "Appears to be a new file."
        }
    }

    static void main(String[] args) {
//        String tokenizer = "anc:opennlp.cloud.tokenizer_1.0.0"
        new DataCreator().path()
    }
}
