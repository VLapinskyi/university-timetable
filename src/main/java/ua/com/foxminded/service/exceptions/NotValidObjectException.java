package ua.com.foxminded.service.exceptions;

public class NotValidObjectException extends RuntimeException {
    private static final long serialVersionUID = 1L;
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
