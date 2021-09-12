package ua.com.foxminded.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ua.com.foxminded.dao.exceptions.DAOException;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Lecturer;
import ua.com.foxminded.mapper.LecturerMapper;
import ua.com.foxminded.settings.SpringDAOTestConfiguration;
import ua.com.foxminded.settings.TestAppender;

@ContextConfiguration(classes = { SpringDAOTestConfiguration.class })
@ExtendWith(SpringExtension.class)
class LecturerDAOTest {
	private final ClassPathResource testData = new ClassPathResource("/Test data.sql");
	private final ClassPathResource testTablesCreator = new ClassPathResource("/Creating tables.sql");
	private final ClassPathResource testDatabaseCleaner = new ClassPathResource("/Clearing database.sql");

	private TestAppender testAppender = new TestAppender();
	@Autowired
	private LecturerDAO lecturerDAO;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private List<Lecturer> expectedLecturers;
	private Connection connection;
	@Mock
	private JdbcTemplate mockedJdbcTemplate;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		connection = jdbcTemplate.getDataSource().getConnection();
		ScriptUtils.executeSqlScript(connection, testTablesCreator);

		expectedLecturers = new ArrayList<>(Arrays.asList(new Lecturer(), new Lecturer(), new Lecturer()));
		List<Integer> indexes = new ArrayList<>(Arrays.asList(1, 2, 3));
		List<String> firstNames = new ArrayList<>(Arrays.asList("Olena", "Ihor", "Vasyl"));
		List<String> lastNames = new ArrayList<>(Arrays.asList("Skladenko", "Zakharchuk", "Dudchenko"));
		List<Gender> gendersForLecturers = new ArrayList<>(Arrays.asList(Gender.FEMALE, Gender.MALE, Gender.MALE));
		List<String> phoneNumbers = new ArrayList<>(Arrays.asList("+380991111111", null, null));
		List<String> emails = new ArrayList<>(Arrays.asList("oskladenko@gmail.com", "i.zakharchuk@gmail.com", null));
		for (int i = 0; i < expectedLecturers.size(); i++) {
			expectedLecturers.get(i).setId(indexes.get(i));
			expectedLecturers.get(i).setFirstName(firstNames.get(i));
			expectedLecturers.get(i).setLastName(lastNames.get(i));
			expectedLecturers.get(i).setGender(gendersForLecturers.get(i));
			expectedLecturers.get(i).setPhoneNumber(phoneNumbers.get(i));
			expectedLecturers.get(i).setEmail(emails.get(i));
		}
	}

	@AfterEach
	void tearDown() throws Exception {
		testAppender.cleanEventList();
		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", jdbcTemplate);
		ScriptUtils.executeSqlScript(connection, testDatabaseCleaner);
	}

	@Test
	void shouldCreateLecturer() {
		Lecturer expectedLecturer = new Lecturer();
		expectedLecturer.setId(1);
		expectedLecturer.setFirstName("First-name");
		expectedLecturer.setLastName("Last-name");
		expectedLecturer.setGender(Gender.MALE);
		expectedLecturer.setPhoneNumber("1233");

		Lecturer testLecturer = new Lecturer();
		testLecturer.setFirstName("First-name");
		testLecturer.setLastName("Last-name");
		testLecturer.setGender(Gender.MALE);
		testLecturer.setPhoneNumber("1233");

		lecturerDAO.create(testLecturer);
		Lecturer actualLecturer = lecturerDAO.findAll().stream().findFirst().get();
		assertEquals(expectedLecturer, actualLecturer);
	}

	@Test
	void shouldFindAllLecturers() {
		ScriptUtils.executeSqlScript(connection, testData);
		List<Lecturer> actualLecturers = lecturerDAO.findAll();
		assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
	}

	@Test
	void shouldFindLecturerById() {
		ScriptUtils.executeSqlScript(connection, testData);
		int checkedId = 2;
		Lecturer expectedLecturer = new Lecturer();
		expectedLecturer.setId(checkedId);
		expectedLecturer.setFirstName("Ihor");
		expectedLecturer.setLastName("Zakharchuk");
		expectedLecturer.setGender(Gender.MALE);
		expectedLecturer.setEmail("i.zakharchuk@gmail.com");
		assertEquals(expectedLecturer, lecturerDAO.findById(checkedId));
	}

	@Test
	void shouldUpdateLecturer() {
		ScriptUtils.executeSqlScript(connection, testData);
		int testId = 2;

		Lecturer testLecturer = new Lecturer();
		testLecturer.setFirstName("Iryna");
		testLecturer.setLastName("Kohan");
		testLecturer.setGender(Gender.FEMALE);
		testLecturer.setEmail("i.kohan@gmail.com");
		testLecturer.setPhoneNumber("+380501234567");

		Lecturer expectedLecturer = new Lecturer();
		expectedLecturer.setId(testId);
		expectedLecturer.setFirstName("Iryna");
		expectedLecturer.setLastName("Kohan");
		expectedLecturer.setGender(Gender.FEMALE);
		expectedLecturer.setEmail("i.kohan@gmail.com");
		expectedLecturer.setPhoneNumber("+380501234567");

		lecturerDAO.update(testId, testLecturer);
		assertEquals(expectedLecturer, lecturerDAO.findById(testId));
	}

	@Test
	void shouldDeleteLecturerById() {
		ScriptUtils.executeSqlScript(connection, testData);
		int deletedId = 2;
		for (int i = 0; i < expectedLecturers.size(); i++) {
			if (expectedLecturers.get(i).getId() == deletedId) {
				expectedLecturers.remove(i);
				i--;
			}
		}
		lecturerDAO.deleteById(deletedId);
		List<Lecturer> actualLecturers = lecturerDAO.findAll();
		assertTrue(expectedLecturers.containsAll(actualLecturers) && actualLecturers.containsAll(expectedLecturers));
	}

	@Test
	void shouldThrowDAOExceptionWhenDataAccessExceptionWhileCreate() {
		Lecturer lecturer = new Lecturer();
		lecturer.setGender(Gender.MALE);
		assertThrows(DAOException.class, () -> lecturerDAO.create(lecturer));
	}

	@Test
	void shouldThrowDAOExceptionWhenDataAccessExceptionWhileFindAll() {
		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		when(mockedJdbcTemplate.query(anyString(), any(LecturerMapper.class), anyString()))
				.thenThrow(QueryTimeoutException.class);
		assertThrows(DAOException.class, () -> lecturerDAO.findAll());
	}

	@Test
	void shouldThrowDAOExceptionWhenEmptyResultDataAccessExceptionWhileFindById() {
		int testId = 1;
		assertThrows(DAOException.class, () -> lecturerDAO.findById(testId));
	}

	@Test
	void shouldThrowDAOExcpetionWhenDataAccessExceptionWhileFindById() {
		int testId = 1;
		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		when(mockedJdbcTemplate.queryForObject(anyString(), any(LecturerMapper.class), anyInt(), anyString()))
				.thenThrow(QueryTimeoutException.class);
		assertThrows(DAOException.class, () -> lecturerDAO.findById(testId));
	}

	@Test
	void shouldThrowDAOExceptionWhenDataAccessExceptionWhileUpdate() {
		int testId = 1;
		Lecturer lecturer = new Lecturer();
		lecturer.setGender(Gender.FEMALE);
		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());
		assertThrows(DAOException.class, () -> lecturerDAO.update(testId, lecturer));
	}

	@Test
	void shouldThrowDAOExceptionWhenDataAccessExceptionWhileDeleteById() {
		int testId = 1;
		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyString());
		assertThrows(DAOException.class, () -> lecturerDAO.deleteById(testId));
	}

	@Test
	void shouldGenerateLogsWhenCreateLecturer() {
		Lecturer testLecturer = new Lecturer();
		testLecturer.setFirstName("Roman");
		testLecturer.setLastName("Dudchenko");
		testLecturer.setGender(Gender.MALE);
		testLecturer.setPhoneNumber("+380998765432");
		testLecturer.setEmail("test@test.com");

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList(
				"Try to insert a new object: " + testLecturer + ".", "The object " + testLecturer + " was inserted."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		lecturerDAO.create(testLecturer);
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenThrowDataAccessExceptionWhileCreate() {
		Lecturer testLecturer = new Lecturer();
		testLecturer.setGender(Gender.MALE);

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList(
				"Try to insert a new object: " + testLecturer + ".", "Can't insert the object: " + testLecturer + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		try {
			lecturerDAO.create(testLecturer);
		} catch (DAOException daoException) {
			// do nothing
		}

		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenFindAllIsEmpty() {
		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.WARN));
		List<String> expectedMessages = new ArrayList<>(
				Arrays.asList("Try to find all objects.", "There are not any objects in the result when findAll."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		lecturerDAO.findAll();

		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenFindAllHasResult() {
		ScriptUtils.executeSqlScript(connection, testData);
		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
		List<String> expectedMessages = new ArrayList<>(
				Arrays.asList("Try to find all objects.", "The result is: " + expectedLecturers + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		lecturerDAO.findAll();
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenThrowDataAccesExceptionWhileFindAll() {

		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		when(mockedJdbcTemplate.query(anyString(), any(LecturerMapper.class), anyString()))
				.thenThrow(QueryTimeoutException.class);

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
		List<String> expectedMessages = new ArrayList<>(
				Arrays.asList("Try to find all objects.", "Can't find all objects."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		try {
			lecturerDAO.findAll();
			verify(mockedJdbcTemplate).query(anyString(), any(LecturerMapper.class), anyString());
		} catch (DAOException daoException) {
			// do nothing
		}
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenFindById() {
		ScriptUtils.executeSqlScript(connection, testData);
		int testId = 2;
		Lecturer expectedLecturer = expectedLecturers.stream().filter(lecturer -> lecturer.getId() == testId)
				.findFirst().get();
		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
				"The result object with id " + testId + " is " + expectedLecturer + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		lecturerDAO.findById(testId);
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenThrowEmptyResultDataAccessExceptionWhileFindById() {
		int testId = 2;
		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
				"There is no result when find an object by id " + testId + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		try {
			lecturerDAO.findById(testId);
		} catch (DAOException daoException) {
			// do nothing
		}

		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenThrowDataAccessExceptionWhileFindById() {
		int testId = 2;

		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		when(mockedJdbcTemplate.queryForObject(anyString(), any(LecturerMapper.class), any()))
				.thenThrow(QueryTimeoutException.class);

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to find an object by id: " + testId + ".",
				"Can't find an object by id " + testId + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		try {
			lecturerDAO.findById(testId);
			verify(mockedJdbcTemplate).queryForObject(anyString(), any(LecturerMapper.class), any());
		} catch (DAOException daoException) {
			// do nothing
		}

		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenUpdate() {
		ScriptUtils.executeSqlScript(connection, testData);
		int testId = 1;
		Lecturer lecturer = new Lecturer();
		lecturer.setFirstName("Roman");
		lecturer.setLastName("Dudchenko");
		lecturer.setGender(Gender.MALE);

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
		List<String> expectedMessages = new ArrayList<>(
				Arrays.asList("Try to update an object " + lecturer + " with id " + testId + ".",
						"The object " + lecturer + " with id " + testId + " was updated."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		lecturerDAO.update(testId, lecturer);
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenThrowDataAccessExceptionWhileUpdate() {
		int testId = 1;
		Lecturer lecturer = new Lecturer();
		lecturer.setFirstName("Roman");
		lecturer.setLastName("Dudchenko");
		lecturer.setGender(Gender.MALE);

		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), (Object) any());

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
		List<String> expectedMessages = new ArrayList<>(
				Arrays.asList("Try to update an object " + lecturer + " with id " + testId + ".",
						"Can't update an object " + lecturer + " with id " + testId + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		try {
			lecturerDAO.update(testId, lecturer);
			verify(mockedJdbcTemplate).update(anyString(), (Object) any());
		} catch (DAOException daoException) {
			// do nothing
		}

		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenDeleteById() {
		ScriptUtils.executeSqlScript(connection, testData);
		int testId = 3;

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.DEBUG));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
				"The object was deleted by id " + testId + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}

		lecturerDAO.deleteById(testId);
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}

	@Test
	void shouldGenerateLogsWhenThrowDataAccessExceptionWhileDeleteById() {
		int testId = 3;

		ReflectionTestUtils.setField(lecturerDAO, "jdbcTemplate", mockedJdbcTemplate);
		doThrow(QueryTimeoutException.class).when(mockedJdbcTemplate).update(anyString(), anyInt(), anyString());

		List<LoggingEvent> expectedLogs = new ArrayList<>(Arrays.asList(new LoggingEvent(), new LoggingEvent()));
		List<Level> expectedLevels = new ArrayList<>(Arrays.asList(Level.DEBUG, Level.ERROR));
		List<String> expectedMessages = new ArrayList<>(Arrays.asList("Try to delete an object by id " + testId + ".",
				"Can't delete an object by id " + testId + "."));
		for (int i = 0; i < expectedLogs.size(); i++) {
			expectedLogs.get(i).setLevel(expectedLevels.get(i));
			expectedLogs.get(i).setMessage(expectedMessages.get(i));
		}
		try {
			lecturerDAO.deleteById(testId);
			verify(mockedJdbcTemplate).update(anyString(), anyInt(), anyString());
		} catch (DAOException daoException) {
			// do nothing
		}
		List<ILoggingEvent> actualLogs = testAppender.getEvents();

		assertEquals(expectedLogs.size(), actualLogs.size());
		for (int i = 0; i < actualLogs.size(); i++) {
			assertEquals(expectedLogs.get(i).getLevel(), actualLogs.get(i).getLevel());
			assertEquals(expectedLogs.get(i).getFormattedMessage(), actualLogs.get(i).getFormattedMessage());
		}
	}
}
