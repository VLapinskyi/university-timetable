package ua.com.foxminded.domain.studying_process;

import ua.com.foxminded.domain.periods.Day;
import ua.com.foxminded.domain.periods.Time;
import ua.com.foxminded.domain.persons.Lecture;
import ua.com.foxminded.domain.university_structure.Group;

public class Lesson {
    private int id;
    private String name;
    private Lecture lecture;
    private Group group;
    private String audience;
    private Day day;
    private Time time;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Lecture getLecture() {
        return lecture;
    }
    
    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public void setAudience(String audience) {
        this.audience = audience;
    }
    
    public Day getDay() {
        return day;
    }
    
    public void setDay(Day day) {
        this.day = day;
    }
    
    public Time getTime() {
        return time;
    }
    
    public void setTime(Time time) {
        this.time = time;
    } 
}
