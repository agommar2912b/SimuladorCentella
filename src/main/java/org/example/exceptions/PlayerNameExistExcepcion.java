package org.example.exceptions;

public class PlayerNameExistExcepcion extends NameExistException {
    public PlayerNameExistExcepcion(String name) {
      super("Player with name " + name + " already exist");
    }
}
