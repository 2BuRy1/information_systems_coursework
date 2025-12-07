package ru.itmo.codetogether.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itmo.codetogether.dto.CommonDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CodeTogetherException.class)
    public ResponseEntity<CommonDto.ErrorResponse> handleCodeTogether(CodeTogetherException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new CommonDto.ErrorResponse(exception.getMessage(), exception.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonDto.ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        FieldError error = exception.getBindingResult().getFieldError();
        String message = error != null ? error.getDefaultMessage() : "Validation error";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CommonDto.ErrorResponse(message, error != null ? error.getField() : null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonDto.ErrorResponse> handleConstraint(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CommonDto.ErrorResponse("Validation error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonDto.ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CommonDto.ErrorResponse("Validation error", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonDto.ErrorResponse> handleGeneric(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CommonDto.ErrorResponse("Internal error", exception.getMessage()));
    }
}
