package com.forgeops.common.exception;

public class InvalidStateTransitionException extends RuntimeException {

    private final String currentState;
    private final String targetState;

    public InvalidStateTransitionException(String entityName, String currentState, String targetState) {
        super(String.format("Invalid state transition for %s from '%s' to '%s'", entityName, currentState, targetState));
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getTargetState() {
        return targetState;
    }
}
