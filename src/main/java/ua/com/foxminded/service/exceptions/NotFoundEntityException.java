package ua.com.foxminded.service.exceptions;

public class NotFoundEntityException extends RuntimeException {
    private RuntimeException exception;
    private String message;
    public NotFoundEntityException(RuntimeException exception, String message) {
        this.exception = exception;
        this.message = message;
    }
    public RuntimeException getException() {
        return exception;
    }
    public void setException(RuntimeException exception) {
        this.exception = exception;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
