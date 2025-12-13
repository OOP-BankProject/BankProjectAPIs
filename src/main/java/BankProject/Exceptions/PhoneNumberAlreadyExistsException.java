package BankProject.Exceptions;

public class PhoneNumberAlreadyExistsException extends RegistrationException{
    public PhoneNumberAlreadyExistsException(String message) {
        super(message);
    }
}
