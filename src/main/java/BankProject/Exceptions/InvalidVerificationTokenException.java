package BankProject.Exceptions;

public class InvalidVerificationTokenException extends RegistrationException{
    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}
