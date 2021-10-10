package ua.com.foxminded.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "people")
public class Student extends Person {
    
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(name = "role")
    private final Role role = Role.STUDENT;
    
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "student_group_id")
    @NotNull(message = "Student must be in some group")
    @Valid
    private Group group;
    
    public Student() {
        
    }

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
