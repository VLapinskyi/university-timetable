package ua.com.foxminded.domain;

import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import ua.com.foxminded.domain.validation.CheckLessonTime;

@CheckLessonTime
@Entity
@Table(name = "lesson_times")
public class LessonTime {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @PositiveOrZero(message = "LessonTime id can't be null")
    private int id;

    @Column(name = "start_time")
    @NotNull(message = "LessonTime's startTime can't be null")
    private LocalTime startTime;

    @Column(name = "end_time")
    @NotNull(message = "LessonTime's endTime can't be null")
    private LocalTime endTime;

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + id;
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LessonTime other = (LessonTime) obj;
        if (endTime == null) {
            if (other.endTime != null)
                return false;
        } else if (!endTime.equals(other.endTime))
            return false;
        if (id != other.id)
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LessonTime [id=" + id + ", " + (startTime != null ? "startTime=" + startTime + ", " : "")
                + (endTime != null ? "endTime=" + endTime : "") + "]";
    }

}
