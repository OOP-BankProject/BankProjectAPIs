package BankProject.Exceptions;

public class InvalidCredentialsException extends RegistrationException{
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
