package org.example.exceptions;

public class UserNameExistException extends NameExistException {
    public UserNameExistException(String name) {
        super("User with name " + name + " already exist");
    }
}
