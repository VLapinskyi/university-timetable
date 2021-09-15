package ua.com.foxminded.service.exceptions;

public class ServiceException extends RuntimeException {
    private RuntimeException exception;
    private String serviceExceptionMessage;

    public ServiceException(String serviceExceptionMessage, RuntimeException exception) {
        this.serviceExceptionMessage = serviceExceptionMessage;
        this.exception = exception;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }

    public String getServiceExceptionMessage() {
        return serviceExceptionMessage;
    }

    public void setServiceExceptionMessage(String serviceExceptionMessage) {
        this.serviceExceptionMessage = serviceExceptionMessage;
    }

}
