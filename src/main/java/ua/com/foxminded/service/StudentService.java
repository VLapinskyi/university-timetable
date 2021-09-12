package ua.com.foxminded.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Student;

@Service
public class StudentService {
	private StudentDAO studentDAO;

	@Autowired
	public StudentService(StudentDAO studentDAO) {
		this.studentDAO = studentDAO;
	}

	public void create(Student student) {
		studentDAO.create(student);
		Optional<Student> createdStudent = studentDAO.findAll().stream().max(Comparator.comparing(Student::getId));
		int studentId = 0;
		if (createdStudent.isPresent()) {
			studentId = createdStudent.get().getId();
		}
		studentDAO.setStudentGroup(student.getGroup().getId(), studentId);
	}

	public List<Student> getAll() {
		List<Student> students = studentDAO.findAll();
		students.stream().forEach(student -> student.setGroup(studentDAO.getStudentGroup(student.getId())));
		return students;
	}

	public Student getById(int studentId) {
		Student student = studentDAO.findById(studentId);
		student.setGroup(studentDAO.getStudentGroup(studentId));
		return student;
	}

	public void update(Student updatedStudent) {
		studentDAO.update(updatedStudent.getId(), updatedStudent);
		studentDAO.setStudentGroup(updatedStudent.getGroup().getId(), updatedStudent.getId());
	}

	public void deleteById(int studentId) {
		studentDAO.deleteById(studentId);
	}

	public List<Student> getStudentsFromGroup(int groupId) {
		List<Student> allStudents = studentDAO.findAll();
		allStudents.stream().forEach(student -> student.setGroup(studentDAO.getStudentGroup(student.getId())));
		return allStudents.stream().filter(student -> student.getGroup().getId() == groupId)
				.collect(Collectors.toList());
	}
}
