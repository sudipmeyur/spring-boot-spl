package com.spl.spl.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.spl.spl.dto.ErrorResponse;
import com.spl.spl.exception.DuplicateResourceException;
import com.spl.spl.exception.PlayerLimitExceededException;
import com.spl.spl.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.error("Resource Not Found Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .details(String.format("The requested %s with identifier '%s' could not be found", 
                        ex.getResourceType(), ex.getResourceId()))
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        
        log.error("Duplicate Resource Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.CONFLICT.value())
                .code("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .details(String.format("A %s with identifier '%s' already exists in the system", 
                        ex.getResourceType(), ex.getResourceId()))
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PlayerLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handlePlayerLimitExceededException(
            PlayerLimitExceededException ex, WebRequest request) {
        
        log.error("Player Limit Exceeded Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("PLAYER_LIMIT_EXCEEDED")
                .message(ex.getMessage())
                .details(String.format("The %s player limit has been reached. Current count: %d, Maximum allowed: %d", 
                        ex.getLimitType(), ex.getCurrentCount(), ex.getMaxAllowed()))
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SplBadRequestException.class)
    public ResponseEntity<ErrorResponse> handleSplBadRequestException(
            SplBadRequestException ex, WebRequest request) {
        
        log.error("SPL Bad Request Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("SPL_BAD_REQUEST")
                .message(ex.getMessage())
                .details("The request could not be processed due to invalid data or business rule violation")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Validation Exception: {}", ex.getMessage(), ex);
        
        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            
            ErrorResponse.ValidationError validationError = ErrorResponse.ValidationError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build();
            
            validationErrors.add(validationError);
        });

        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .details("Please check the provided data and correct the validation errors")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .validationErrors(validationErrors)
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.error("Illegal Argument Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .details("The provided argument is invalid or not acceptable")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        log.error("Runtime Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details("Please contact support if the problem persists")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Generic Exception: {}", ex.getMessage(), ex);
        
        ErrorResponse.ErrorData errorData = ErrorResponse.ErrorData.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details("Please contact support if the problem persists")
                .timestamp(LocalDateTime.now())
                .path(getPath(request))
                .build();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(errorData)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}