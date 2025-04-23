package org.example.exceptions;

public class UserNotFoundNameException extends NameExistException {
  public UserNotFoundNameException(String name) {
    super("User with name " + name + " not found");
  }
}
