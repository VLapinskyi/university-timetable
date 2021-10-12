package ua.com.foxminded.repositories.exceptions;

public class RepositoryException extends RuntimeException {
    private Exception exception;
    private String repositoryExceptionMessage;

    public RepositoryException(String repositoryExceptionMessage, Exception exception) {
        this.repositoryExceptionMessage = repositoryExceptionMessage;
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getRepositoryExceptionMessage() {
        return repositoryExceptionMessage;
    }

    public void setRepositoryExceptionMessage(String repositoryExceptionMessage) {
        this.repositoryExceptionMessage = repositoryExceptionMessage;
    }
}
