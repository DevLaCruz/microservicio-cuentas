package com.nttdata.AccountMs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CuentaException.class)
    public ResponseEntity<Object> handleCuentaException(CuentaException e) {
        // Crear un objeto de respuesta para el error
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
