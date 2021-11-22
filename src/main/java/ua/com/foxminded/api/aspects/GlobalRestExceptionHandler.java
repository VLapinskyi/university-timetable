package ua.com.foxminded.api.aspects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> restExceptionHandler(ResponseStatusException exception) {
        return new ResponseEntity<>(exception.getMessage(), exception.getStatus());
    }
}
