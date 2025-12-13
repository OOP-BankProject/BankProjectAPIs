package BankProject.Exceptions;

public class OtpAttemptsExceededException extends RegistrationException{
    public OtpAttemptsExceededException(String message) {
        super(message);
    }
}
