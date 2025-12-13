package BankProject.Exceptions;

public class InvalidSessionTokenException extends RegistrationException{
    public InvalidSessionTokenException(String message) {
        super(message);
    }
}
