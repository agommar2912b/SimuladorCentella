package org.example.exceptions;

public class TeamNotFoundException extends NotFoundException{
    public TeamNotFoundException(Long id) {
        super("Team with id " + id + " not found");
    }
}
