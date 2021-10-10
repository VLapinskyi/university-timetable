package ua.com.foxminded.dao.exceptions;

public class DAOException extends RuntimeException {
    private Exception exception;
    private String daoExceptionMessage;

    public DAOException(String daoExceptionMessage, Exception exception) {
        this.daoExceptionMessage = daoExceptionMessage;
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getDaoExceptionMessage() {
        return daoExceptionMessage;
    }

    public void setDaoExceptionMessage(String daoExceptionMessage) {
        this.daoExceptionMessage = daoExceptionMessage;
    }
}
