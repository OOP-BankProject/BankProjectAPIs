package BankProject.Exceptions;

public class PasswordMismatchException extends RegistrationException{
    public PasswordMismatchException(String message) {
        super(message);
    }
}
