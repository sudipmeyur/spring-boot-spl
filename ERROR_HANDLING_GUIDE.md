# SPL Application Error Handling Guide

## Overview

This document describes the comprehensive error handling implementation for the SPL (Sports Premier League) application. The error handling system provides structured, consistent error responses to clients with appropriate HTTP status codes.

## Error Response Structure

All errors follow a consistent JSON structure:

```json
{
  "error": {
    "status": 400,
    "code": "PLAYER_LIMIT_EXCEEDED",
    "message": "Maximum free player limit reached. Current: 5, Max allowed: 4",
    "details": "The free player limit has been reached. Current count: 5, Maximum allowed: 4",
    "timestamp": "2026-01-25T21:06:32.123456",
    "path": "/api/player-teams",
    "validationErrors": [
      {
        "field": "playerCode",
        "message": "Player code cannot be null",
        "rejectedValue": null
      }
    ]
  }
}
```

## Exception Types

### 1. PlayerLimitExceededException
- **HTTP Status**: 400 (Bad Request)
- **Error Code**: `PLAYER_LIMIT_EXCEEDED`
- **Usage**: When team exceeds maximum allowed players (free or RTM)
- **Example**: Adding a 5th free player when only 4 are allowed

### 2. SplBadRequestException
- **HTTP Status**: 400 (Bad Request)
- **Error Code**: `SPL_BAD_REQUEST`
- **Usage**: General business rule violations
- **Example**: Invalid data or business logic errors

### 3. ResourceNotFoundException
- **HTTP Status**: 404 (Not Found)
- **Error Code**: `RESOURCE_NOT_FOUND`
- **Usage**: When requested resource doesn't exist
- **Example**: Player or team not found

### 4. DuplicateResourceException
- **HTTP Status**: 409 (Conflict)
- **Error Code**: `DUPLICATE_RESOURCE`
- **Usage**: When trying to create a resource that already exists
- **Example**: Creating a player with existing code

### 5. Validation Errors
- **HTTP Status**: 400 (Bad Request)
- **Error Code**: `VALIDATION_ERROR`
- **Usage**: Bean validation failures
- **Example**: Missing required fields, invalid formats

## Implementation Details

### Global Exception Handler
The `GlobalExceptionHandler` class uses `@RestControllerAdvice` to handle exceptions globally across all controllers.

### Exception Hierarchy
```
Exception
├── RuntimeException
│   ├── IllegalArgumentException
│   │   └── SplBadRequestException
│   │       ├── PlayerLimitExceededException
│   │       └── DuplicateResourceException
│   └── ResourceNotFoundException
└── MethodArgumentNotValidException (Spring validation)
```

### Error Codes Reference

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `PLAYER_LIMIT_EXCEEDED` | 400 | Team player limits exceeded |
| `SPL_BAD_REQUEST` | 400 | General business rule violation |
| `RESOURCE_NOT_FOUND` | 404 | Requested resource not found |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists |
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `INVALID_ARGUMENT` | 400 | Invalid method argument |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

## Usage Examples

### Client Error Handling

```javascript
// Example client-side error handling
fetch('/api/player-teams', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(playerData)
})
.then(response => {
  if (!response.ok) {
    return response.json().then(errorData => {
      throw new Error(errorData.error.message);
    });
  }
  return response.json();
})
.catch(error => {
  // Handle specific error codes
  if (error.message.includes('PLAYER_LIMIT_EXCEEDED')) {
    showPlayerLimitError();
  } else if (error.message.includes('VALIDATION_ERROR')) {
    showValidationErrors();
  }
});
```

### Service Layer Usage

```java
// Throwing specific exceptions in service layer
if (season.getMaxFreeAllowed() <= teamSeason.getTotalFreeUsed()) {
    throw new PlayerLimitExceededException("free", 
        teamSeason.getTotalFreeUsed(), 
        season.getMaxFreeAllowed());
}

// For resource not found
Player player = playerRepository.findByCode(code);
if (player == null) {
    throw new ResourceNotFoundException("Player", code);
}
```

## Benefits

1. **Consistent Error Format**: All errors follow the same JSON structure
2. **Detailed Information**: Errors include context, timestamps, and paths
3. **Proper HTTP Status Codes**: Appropriate status codes for different error types
4. **Client-Friendly**: Structured data allows for better client-side error handling
5. **Logging**: All errors are logged with appropriate levels
6. **Extensible**: Easy to add new exception types and handlers

## Best Practices

1. Use specific exception types for different error scenarios
2. Include relevant context in error messages
3. Log errors appropriately (error level for server issues, warn for client issues)
4. Don't expose sensitive information in error messages
5. Use validation annotations for input validation
6. Handle edge cases gracefully

## Testing Error Scenarios

To test the error handling, you can:

1. **Player Limit Exceeded**: Try adding more free players than allowed
2. **Validation Error**: Send request with missing required fields
3. **Resource Not Found**: Use non-existent player or team codes
4. **Duplicate Resource**: Try creating resources with existing identifiers