package net.gaborszita.realisticelevator.elevator.exceptions;

public class ElevatorAlreadyExistsException extends IllegalArgumentException {
  public ElevatorAlreadyExistsException(String message) {
    super(message);
  }
}
