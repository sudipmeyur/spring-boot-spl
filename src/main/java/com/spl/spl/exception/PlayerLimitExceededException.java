package com.spl.spl.exception;

public class PlayerLimitExceededException extends SplBadRequestException {

    private static final long serialVersionUID = 1L;
    
    private final String limitType;
    private final int currentCount;
    private final int maxAllowed;

    public PlayerLimitExceededException(String limitType, int currentCount, int maxAllowed) {
        super(String.format("Maximum %s player limit reached. Current: %d, Max allowed: %d", 
              limitType, currentCount, maxAllowed));
        this.limitType = limitType;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
    }

    public String getLimitType() {
        return limitType;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }
}