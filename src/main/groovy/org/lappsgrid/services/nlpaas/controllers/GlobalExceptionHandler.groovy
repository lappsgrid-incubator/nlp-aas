package org.lappsgrid.services.nlpaas.controllers

import org.lappsgrid.services.nlpaas.json.Serializer
import org.lappsgrid.services.nlpaas.util.HTML
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

/**
 *
 */
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception)
    ResponseEntity<String> handleException(Exception e, WebRequest request) {
        String body
        if ('text/html' == request.getHeader('Accept')) {
            body = HTML.render('Error') {
                h1 'There was an error processing your request.'
                p e.message
                p 'There may be more information available in the log files.'
            }
        }
        else {
            Map data = [
                    message: e.message
            ]
            body = Serializer.toJson(data)
        }
        return ResponseEntity.status(500).body(body)
    }
}
