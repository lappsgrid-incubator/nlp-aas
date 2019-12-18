package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.services.nlpaas.util.HTML
import org.lappsgrid.services.nlpaas.util.Time
import org.lappsgrid.services.nlpaas.dto.JobDescription
import org.lappsgrid.services.nlpaas.dto.JobRequest
import org.lappsgrid.services.nlpaas.model.LappsgridService
import org.lappsgrid.services.nlpaas.model.ServiceRegistry
import org.lappsgrid.services.nlpaas.work.ManagerService
import org.lappsgrid.services.nlpaas.work.StorageService
import org.lappsgrid.services.nlpaas.work.WorkOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import java.time.ZonedDateTime

import static org.lappsgrid.discriminator.Discriminators.*

/**
 *
 */
@Slf4j("logger")
@RestController
@RequestMapping("/job")
class JobController {

    @Autowired
    private ManagerService manager
    @Autowired
    private ServiceRegistry registry
    @Autowired
    private StorageService storage

//    @GetMapping(path="/test")
//    ResponseEntity getTest() {
//        return ResponseEntity.ok().header('Location', '/download/123').build()
//    }
//
//    @GetMapping(path="/ping/{token}")
//    ResponseEntity getPing(@PathVariable String token) {
//        return ResponseEntity.ok('pong ' + token)
//    }

//    @PostMapping(path="/validate", produces="application/json", consumes="application/json")
//    ResponseEntity validate(@RequestParam String json) {
//        return ResponseEntity.ok(json)
//    }

    @GetMapping(produces = 'text/html')
    String getListHtml() {
           return HTML.render {
            table {
                tr {
                    th 'Job ID'
                    th 'Submitted'
                    th 'Status'
                }
                manager.jobs().each { job ->
                    tr {
                        td { a href:'/job/'+job.id, job.id }
                        td job.submittedAt
                        td job.status
                    }
                }
            }
        }
    }

    @GetMapping(path='/{id}', produces = 'application/json')
    ResponseEntity checkStatus(@PathVariable String id) {
        if (id == 'list') {
            return ResponseEntity.ok(manager.list())
        }
        JobDescription description = manager.get(id)
        if (description == null) {
            return ResponseEntity.status(404).body("No job with id " + id + '\n')
        }
        description.calculateElapsed()
        return ResponseEntity.ok().body(description)
    }

    @GetMapping(path='/{id}', produces = 'text/html')
    ResponseEntity<String> getJobHtml(@PathVariable String id) {
        JobDescription description = manager.get(id)
        if (description == null) {
            String html = HTML.render {
                h1 '404 - Not Found'
                p "There is not job with ID ${id}"
            }
            return ResponseEntity.status(404).body(html)
        }
        description.calculateElapsed()
        String body = HTML.render('Job Description') {
            h1 description.id
            table {
                tr {
                    th 'Name'
                    th 'Value'
                }
                tr {
                    td 'Status'
                    td description.status
                }
                if (description.message) {
                    tr {
                        td 'Message'
                        td description.message
                    }
                }
                if (description.submittedAt) {
                    tr {
                        td "Submitted"
                        td description.submittedAt
                    }

                }
                if (description.startedAt) {
                    tr {
                        td 'Started'
                        td description.startedAt
                    }
                }
                if (description.stoppedAt) {
                    tr {
                        td "Stopped"
                        td description.stoppedAt
                    }

                }
                if (description.finishedAt) {
                    tr {
                        td "Finished"
                        td description.finishedAt
                    }

                }
                if (description.elapsed) {
                    tr {
                        td "Elapsed"
                        td description.elapsed
                    }

                }
                if (description.resultUrl) {
                    tr {
                        td 'Download'
                        td {
                            a href:description.resultUrl, description.resultUrl
                        }
                    }
                }
                tr {
                    td(colspan:2) {
                        div(id:'delete-message-div', class:'hide') {
                            p id:'delete-message', 'Job deleted.'
                        }
                        //a id:'delete-link', href:'/job/'+description.id, class:'button', 'Delete'
                        button(id:'delete-button', type:'button', onclick:"delete_job('$description.id')", 'Delete')
                    }
                }
            }
        }
        return ResponseEntity.ok(body)
    }

    @DeleteMapping(path="/{id}", produces = 'text/plain')
    ResponseEntity deleteJob(@PathVariable String id) {
        if (manager.remove(id)) {
            logger.info("Removed job {}", id)
            return ResponseEntity.ok("Job $id cancelled.")
        }
        logger.warn("Unable to remove job {}", id)
        return ResponseEntity.status(404).body('No such processing job.')
    }

    /*
    @DeleteMapping(path='/{id}', produces = 'text/html')
    ResponseEntity<String> deleteJobHtml(@PathVariable String id) {
        logger.info("Attempting to delele job {}", id)
        if (manager.remove(id)) {
            logger.debug("Job deleted.")
            String body = HTML.render('Job Deleted') {
                h1 'Job Deleted'
                p "The job with ID $id has been removed.  If the job has completed then the file is still available for download from " + mkp.yeild("<a href='/download/$id'>/download/$id</a>")
            }
            return ResponseEntity.ok(body)
        }
        logger.warn("There is no job with id {}", id)
        String body = HTML.render('Not Found') {
            h1 '404 - Not Found'
            p "There is no job with ID $id or it has been deleted by another process."
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }
    */

}

