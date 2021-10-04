package ua.com.foxminded.dao.exceptions;

import org.springframework.dao.DataAccessException;

public class DAOException extends RuntimeException {
    private DataAccessException dataAccessException;
    private String daoExceptionMessage;

    public DAOException(String daoExceptionMessage, DataAccessException dataAccessException) {
        this.daoExceptionMessage = daoExceptionMessage;
        this.dataAccessException = dataAccessException;
    }

    public DataAccessException getDataAccessException() {
        return dataAccessException;
    }

    public void setDataAccessException(DataAccessException dataAccessException) {
        this.dataAccessException = dataAccessException;
    }

    public String getDaoExceptionMessage() {
        return daoExceptionMessage;
    }

    public void setDaoExceptionMessage(String daoExceptionMessage) {
        this.daoExceptionMessage = daoExceptionMessage;
    }
}
