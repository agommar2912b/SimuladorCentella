package org.example.exceptions;

public class TeamNameNotExistException extends NameExistException {
    public TeamNameNotExistException(String name) {
      super("Team with name dont exist " + name);
    }
}
