package com.throttling.task.controller;

import com.throttling.task.aspect.RateLimit;
import com.throttling.task.exception.RateLimitedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AppController {

    @GetMapping
    @RateLimit
    public ResponseEntity<HttpStatus> get() {
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<HttpStatus> handleConflict(RateLimitedException e, HttpServletResponse response) {
        return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
    }

}
