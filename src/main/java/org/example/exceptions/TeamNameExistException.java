package org.example.exceptions;

public class TeamNameExistException extends NameExistException {
    public TeamNameExistException(String name) {
      super("Team with name " + name + " already exist");
    }
}
