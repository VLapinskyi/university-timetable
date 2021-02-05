package ua.com.foxminded.ui;

import java.time.LocalTime;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.format.datetime.joda.LocalTimeParser;

import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.dao.LessonTimeDAO;
import ua.com.foxminded.dao.StudentDAO;
import ua.com.foxminded.domain.Faculty;
import ua.com.foxminded.domain.Gender;
import ua.com.foxminded.domain.Group;
import ua.com.foxminded.domain.LessonTime;
import ua.com.foxminded.domain.Student;
import ua.com.foxminded.settings.SpringConfiguration;

public class Launcher {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        //StudentDAO dao = context.getBean(StudentDAO.class);
        /*
         * Student student = new Student(); student.setFirstName("Valentyn");
         * student.setLastName("Lapinskyi"); student.setGender(Gender.MALE);
         * student.setPhoneNumber("+380960680730");
         * student.setEmail("valentinlapinskiy@gmail.com"); student.setGroupId(1);
         * //dao.create(student); System.out.println(dao.findById(3));
         */
        GroupDAO groupDAO = context.getBean(GroupDAO.class);
        Group group = new Group();
        group.setName("test");
        groupDAO.create(group);
    }
}
