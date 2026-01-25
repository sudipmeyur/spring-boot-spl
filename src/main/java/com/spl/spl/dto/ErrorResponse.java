package com.spl.spl.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    @JsonView(Views.Base.class)
    private ErrorData error;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorData {
        
        @JsonView(Views.Base.class)
        private int status;
        
        @JsonView(Views.Base.class)
        private String code;
        
        @JsonView(Views.Base.class)
        private String message;
        
        @JsonView(Views.Base.class)
        private String details;
        
        @JsonView(Views.Base.class)
        private LocalDateTime timestamp;
        
        @JsonView(Views.Base.class)
        private String path;
        
        @JsonView(Views.Base.class)
        private List<ValidationError> validationErrors;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        
        @JsonView(Views.Base.class)
        private String field;
        
        @JsonView(Views.Base.class)
        private String message;
        
        @JsonView(Views.Base.class)
        private Object rejectedValue;
    }
}