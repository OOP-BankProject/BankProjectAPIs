package BankProject.Exceptions;

public class EmailAlreadyExistsException extends RegistrationException{
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
