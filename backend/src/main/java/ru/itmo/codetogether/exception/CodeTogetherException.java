package ru.itmo.codetogether.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CodeTogetherException extends RuntimeException {

    private final HttpStatus status;
    private final String details;

    public CodeTogetherException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public CodeTogetherException(HttpStatus status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}
