
package com.nikolas.deploymenttracker.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String name) {
        super("Application already exists: " + name);
    }
}
