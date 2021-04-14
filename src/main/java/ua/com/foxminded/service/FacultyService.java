package ua.com.foxminded.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.service.exceptions.NotValidObjectException;
import ua.com.foxminded.service.exceptions.ServiceException;

@Service
public class FacultyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacultyService.class);
    private FacultyDAO facultyDAO;

    @Autowired
    public FacultyService (FacultyDAO facultyDAO) {
        this.facultyDAO = facultyDAO;
    }

    public void createFaculty(Faculty faculty) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to create new faculty: {}.", faculty);
        }

        try {
            validateFaculty(faculty);

            if (faculty.getId() != 0) {
                NotValidObjectException notValidException = new NotValidObjectException("The faculty has setted id and it is different from zero.");
                LOGGER.error("The faculty {} has setted id {} and it is different from zero.", faculty, faculty.getId(), notValidException);
                throw notValidException;
            }

            facultyDAO.create(faculty);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty {} was created.", faculty);
            }

        } catch (NotValidObjectException notValidObjectException) {
            throw new ServiceException("The faculty is not valid for creating", notValidObjectException);
        } catch (DAOException daoException) {
            LOGGER.error("Can't create faculty {}.", faculty, daoException);
            throw new ServiceException("Can't create faculty.", daoException);
        }
    }

    public List<Faculty> getAllFaculties() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get all faculties.");
        }

        try {            
            List<Faculty> resultFaculties = facultyDAO.findAll();

            if (resultFaculties.isEmpty()) {
                LOGGER.warn("There are not any faculties in the result.");
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The result is: {}.", resultFaculties);
                }
            }

            return resultFaculties;

        } catch (DAOException daoException) {
            LOGGER.error("Can't get all faculties.", daoException);
            throw new ServiceException("Can't get all faculties.", daoException);
        }

    }

    public Faculty getFacultyById(int facultyId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to get faculty by id {}.", facultyId);
        }
        try {
            Faculty resultFaculty = facultyDAO.findById(facultyId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result faculty with id {} is {}.", facultyId, resultFaculty);
            }
            return resultFaculty;
        } catch (DAOException daoException) {
            LOGGER.error("Can't get faculty by id {}.", facultyId, daoException);
            throw new ServiceException("Can't get faculty by id.", daoException);
        }
    }

    public void updateFaculty (Faculty updatedFaculty) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to update faculty {}.", updatedFaculty);
        }

        int facultyId = updatedFaculty.getId();

        try {
            if (facultyId < 1) {
                NotValidObjectException notValidObjectException = new NotValidObjectException("Updated faculty has incorect id.");
                LOGGER.error("The updated faculty {} has incorrect id {}.", updatedFaculty, facultyId, notValidObjectException);
                throw notValidObjectException;
            }

            validateFaculty(updatedFaculty);

            facultyDAO.update(facultyId, updatedFaculty);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty {} was updated.", updatedFaculty);
            }

        } catch (NotValidObjectException notValidObjectException) {
            throw new ServiceException("Not valid updated faculty.", notValidObjectException);
        } catch (DAOException daoException) {
            LOGGER.error("Can't update faculty {}.", updatedFaculty, daoException);
            throw new ServiceException("Can't update faculty.", daoException);
        }
    }

    public void deleteFacultyById (int facultyId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Try to delete faculty by id {}.", facultyId);
        }
        try {
            facultyDAO.deleteById(facultyId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The faculty with id {} was deleted.", facultyId);
            }

        } catch (DAOException daoException) {
            LOGGER.error("Can't delete faculty by id {}.", facultyId);
            throw new ServiceException("Can't delete faculty by id.", daoException);
        }
    }

    void validateFaculty (Faculty faculty) {
        if (faculty == null) {
            NotValidObjectException notValidException = new NotValidObjectException("The faculty is null");
            LOGGER.error("The faculty {} is null.", faculty, notValidException);
            throw notValidException;
        }

        if (faculty.getName() == null || faculty.getName().trim().length() < 2) {
            NotValidObjectException notValidException = new NotValidObjectException("The faculty's name is not valid.");
            LOGGER.error("The faculty's name {} is not valid.", faculty.getName(), notValidException);
            throw notValidException;
        }
    }
}
