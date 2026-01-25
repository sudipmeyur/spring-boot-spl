package com.spl.spl.exception;

public class DuplicateResourceException extends SplBadRequestException {

    private static final long serialVersionUID = 1L;
    
    private final String resourceType;
    private final String resourceId;

    public DuplicateResourceException(String resourceType, String resourceId) {
        super(String.format("%s already exists with identifier: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}