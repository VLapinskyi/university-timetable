package ua.com.foxminded.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class Student extends Person {
    @NotNull(message = "Student must be in some group")
    @Valid
    private Group group;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Student other = (Student) obj;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Student [" + (group != null ? "group=" + group + ", " : "")
                + (super.toString() != null ? "toString()=" + super.toString() : "") + "]";
    }
    
    
}
