package ua.com.foxminded.service.exceptions;

public class NotValidObjectException extends RuntimeException {
    private String message;
    
    public NotValidObjectException (String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
