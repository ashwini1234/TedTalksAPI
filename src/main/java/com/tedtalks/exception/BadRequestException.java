package com.tedtalks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.List;
import java.util.Map;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    private final List<Map<String, Object>> errors;

    public BadRequestException(String message, List<Map<String, Object>> errors) {
        super(message);
        this.errors = errors;
    }

    public List<Map<String, Object>> getErrors() {
        return errors;
    }
}

