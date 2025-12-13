package BankProject.Exceptions;

public class InvalidPhoneNumberException extends RegistrationException{
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}
