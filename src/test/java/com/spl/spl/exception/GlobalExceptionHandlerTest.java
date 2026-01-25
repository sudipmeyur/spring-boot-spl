package com.spl.spl.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import com.spl.spl.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/player-teams");
    }

    @Test
    void testHandlePlayerLimitExceededException() {
        // Given
        PlayerLimitExceededException exception = new PlayerLimitExceededException("free", 5, 4);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handlePlayerLimitExceededException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PLAYER_LIMIT_EXCEEDED", response.getBody().getError().getCode());
        assertEquals(400, response.getBody().getError().getStatus());
        assertEquals("Maximum free player limit reached. Current: 5, Max allowed: 4", 
                response.getBody().getError().getMessage());
    }

    @Test
    void testHandleSplBadRequestException() {
        // Given
        SplBadRequestException exception = new SplBadRequestException("Invalid player data");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleSplBadRequestException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SPL_BAD_REQUEST", response.getBody().getError().getCode());
        assertEquals(400, response.getBody().getError().getStatus());
        assertEquals("Invalid player data", response.getBody().getError().getMessage());
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Player", "P001");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getError().getCode());
        assertEquals(404, response.getBody().getError().getStatus());
        assertEquals("Player not found with identifier: P001", 
                response.getBody().getError().getMessage());
    }

    @Test
    void testHandleDuplicateResourceException() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Player", "P001");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDuplicateResourceException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DUPLICATE_RESOURCE", response.getBody().getError().getCode());
        assertEquals(409, response.getBody().getError().getStatus());
        assertEquals("Player already exists with identifier: P001", 
                response.getBody().getError().getMessage());
    }
}