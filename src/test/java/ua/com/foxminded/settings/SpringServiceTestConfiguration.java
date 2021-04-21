package ua.com.foxminded.settings;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import ua.com.foxminded.dao.FacultyDAO;
import ua.com.foxminded.dao.GroupDAO;
import ua.com.foxminded.dao.LecturerDAO;
import ua.com.foxminded.dao.LessonDAO;
import ua.com.foxminded.dao.LessonTimeDAO;
import ua.com.foxminded.dao.StudentDAO;

@Configuration
@ComponentScan("ua.com.foxminded.service")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringServiceTestConfiguration {

    @Bean
    public FacultyDAO facultyDAO () {
        return Mockito.mock(FacultyDAO.class);
    }
    
    @Bean
    public GroupDAO groupDAO () {
        return Mockito.mock(GroupDAO.class);
    }
    
    @Bean
    public LecturerDAO lecturerDAO () {
        return Mockito.mock(LecturerDAO.class);
    }
    
    @Bean
    public LessonDAO lessonDAO () {
        return Mockito.mock(LessonDAO.class);
    }
    
    @Bean
    public LessonTimeDAO lessonTimeDAO () {
        return Mockito.mock(LessonTimeDAO.class);
    }
    
    @Bean
    public StudentDAO studentDAO () {
        return Mockito.mock(StudentDAO.class);
    }
}
