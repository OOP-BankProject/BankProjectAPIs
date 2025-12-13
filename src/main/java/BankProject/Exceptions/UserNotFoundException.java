package BankProject.Exceptions;

public class UserNotFoundException extends RegistrationException {
    public UserNotFoundException(String message) {
        super(message);
    }
}