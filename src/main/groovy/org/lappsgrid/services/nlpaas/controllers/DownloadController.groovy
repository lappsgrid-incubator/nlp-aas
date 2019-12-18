package org.lappsgrid.services.nlpaas.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.services.nlpaas.dto.JobDescription
import org.lappsgrid.services.nlpaas.work.ManagerService
import org.lappsgrid.services.nlpaas.work.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

/**
 *
 */
@RestController
@RequestMapping(path="/download")
@Slf4j("logger")
class DownloadController {
    @Autowired
    private StorageService storage
    @Autowired
    private ManagerService manager

    @GetMapping(path="/{id}", produces = 'application/json')
    ResponseEntity download(@PathVariable String id) {

        String data = storage.get(id)
        if (data != null) {
            return ResponseEntity.ok().body(data)
        }
        JobDescription description = manager.get(id)
        if (description == null) {
            return ResponseEntity.status(404).body("No job or download available for id " + id + "\n")
        }
        if (description.finishedAt && !storage.exists(id)) {
            // A job was completed but the result no longer exists.
            return ResponseEntity.status(HttpStatus.GONE).body(description)
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(new URI('/job/'+id)).body(description)
    }

    @DeleteMapping(path='/{id}')
    ResponseEntity deleteDownload(@PathVariable String id) {
        if (storage.exists(id)) {
            storage.remove(id)
            return ResponseEntity.ok("Download deleted.\n")
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No such download found.\n")
    }

}
