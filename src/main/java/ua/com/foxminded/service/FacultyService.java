package ua.com.foxminded.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.exceptions.NoResultException;
import ua.com.foxminded.service.exceptions.NotValidObjectException;

@Service
public class FacultyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacultyService.class);
    private FacultyDAO facultyDAO;

    @Autowired
    public FacultyService (FacultyDAO facultyDAO) {
        this.facultyDAO = facultyDAO;
    }

    public void createFaculty(Faculty faculty) {
        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create new faculty: {}.", faculty);
            }

            validateFaculty(faculty);

            if (faculty.getId() != 0) {
                NotValidObjectException exception = new NotValidObjectException("The faculty has setted id and it is different from zero.");
                LOGGER.warn("The faculty has setted id {} and it is different from zero.", faculty.getId(), exception);
                throw exception;
            }

            facultyDAO.create(faculty);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty {} was created.", faculty);
            }

        } catch (DataAccessException exception) {
            //DAOException daoException =  new DAOException("Can't insert faculty into database.", exception);
           // LOGGER.error("Can't insert faculty {} into database", faculty, daoException);
            //throw daoException;
        }
    }

    public List<Faculty> getAllFaculties() {
        try {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to get all faculties.");
            }

            List<Faculty> resultFaculties = facultyDAO.findAll();
            
            if (resultFaculties.isEmpty()) {
                String message = "There are not any faculties in the result";
                NoResultException exception = new NoResultException(message);
                LOGGER.warn(message, exception);
                throw exception;
            }
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is: {}.", resultFaculties);
            }
            
            return resultFaculties;

        } catch (DataAccessException exception) {
            //DAOException daoException =  new DAOException("Can't get all faculties.", exception);
            //LOGGER.error("Can't get all faculties", daoException);
            throw exception;
        }
        
    }

    public Faculty getFacultyById(int facultyId) {
        return facultyDAO.findById(facultyId);
    }

    public void updateFaculty (int facultyId, Faculty updatedFaculty) {
        facultyDAO.update(facultyId, updatedFaculty);
    }

    public void deleteFacultyById (int facultyId) {
        facultyDAO.deleteById(facultyId);
    }

    private void validateFaculty (Faculty faculty) {
        if (faculty == null) {
            NotValidObjectException exception = new NotValidObjectException("The faculty is null");
            LOGGER.error("The faculty {} is null.", faculty, exception);
            throw exception;
        }

        if (faculty.getName() == null || faculty.getName().trim().length() < 2) {
            NotValidObjectException exception = new NotValidObjectException("The faculty's name is not valid.");
            LOGGER.error("The faculty's name {} is not valid.", faculty.getName(), exception);
            throw exception;
        }
    }
}
