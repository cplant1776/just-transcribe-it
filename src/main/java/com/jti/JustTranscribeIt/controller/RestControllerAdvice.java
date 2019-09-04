package com.jti.JustTranscribeIt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;

@ControllerAdvice
public class RestControllerAdvice {
    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<HttpStatus> handleDeleteAuthentication(AuthenticationException dae) {
        // Log error
        HttpStatus error = HttpStatus.FORBIDDEN;
        return new ResponseEntity<HttpStatus>(error);

    }
}
