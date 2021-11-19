package ua.com.foxminded.service.exceptions;

public class NotFoundEntityException extends RuntimeException {
    private RuntimeException exception;
    
    public NotFoundEntityException () {
        
    }

    public NotFoundEntityException(RuntimeException exception, String message) {
        super(message);
        this.exception = exception;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }
}
