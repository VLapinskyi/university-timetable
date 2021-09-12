package ua.com.foxminded.domain;

import java.time.DayOfWeek;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class Lesson {
	@PositiveOrZero(message = "Lesson id can't be negative")
	private int id;

	@NotNull(message = "Lesson name can't be null")
	@Pattern(regexp = "\\S{2,}.*", message = "Lesson name must have at least two symbols and start with non-white space")
	private String name;

	@NotNull(message = "Lesson lecturer can't be null")
	@Valid
	private Lecturer lecturer;

	@NotNull(message = "Lesson group can't be null")
	@Valid
	private Group group;

	@NotNull(message = "Lesson audience can't be null")
	@Pattern(regexp = "\\S{2,}.*", message = "Lesson audience must have at least two symbols and start with non-white space")
	private String audience;

	@NotNull(message = "Lesson day can't be null")
	private DayOfWeek day;

	@NotNull(message = "Lesson time can't be null")
	@Valid
	private LessonTime lessonTime;

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

	public Lecturer getLecturer() {
		return lecturer;
	}

	public void setLecturer(Lecturer lecturer) {
		this.lecturer = lecturer;
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

	public DayOfWeek getDay() {
		return day;
	}

	public void setDay(DayOfWeek day) {
		this.day = day;
	}

	public LessonTime getLessonTime() {
		return lessonTime;
	}

	public void setLessonTime(LessonTime lessonTime) {
		this.lessonTime = lessonTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((audience == null) ? 0 : audience.hashCode());
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + id;
		result = prime * result + ((lecturer == null) ? 0 : lecturer.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((lessonTime == null) ? 0 : lessonTime.hashCode());
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
		Lesson other = (Lesson) obj;
		if (audience == null) {
			if (other.audience != null)
				return false;
		} else if (!audience.equals(other.audience))
			return false;
		if (day != other.day)
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (id != other.id)
			return false;
		if (lecturer == null) {
			if (other.lecturer != null)
				return false;
		} else if (!lecturer.equals(other.lecturer))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (lessonTime == null) {
			if (other.lessonTime != null)
				return false;
		} else if (!lessonTime.equals(other.lessonTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Lesson [id=" + id + ", " + (name != null ? "name=" + name + ", " : "")
				+ (lecturer != null ? "lecturer=" + lecturer + ", " : "")
				+ (group != null ? "group=" + group + ", " : "")
				+ (audience != null ? "audience=" + audience + ", " : "") + (day != null ? "day=" + day + ", " : "")
				+ (lessonTime != null ? "lessonTime=" + lessonTime : "") + "]";
	}

}