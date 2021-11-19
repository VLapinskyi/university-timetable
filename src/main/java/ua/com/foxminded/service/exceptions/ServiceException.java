package ua.com.foxminded.service.exceptions;

public class ServiceException extends RuntimeException {
    private RuntimeException exception;
    
    public ServiceException() {

    }

    public ServiceException(String serviceExceptionMessage, RuntimeException exception) {
        super(serviceExceptionMessage);
        this.exception = exception;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }
}
