package org.example.exceptions;

public class PlayerNotFoundException extends NotFoundException {
    public PlayerNotFoundException(Long id) {
      super("Player with id " + id + " not found");
    }
}
