package ua.com.foxminded.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "people")
public class Lecturer extends Person {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private final Role role = Role.LECTURER;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Lecturer [" + (super.toString() != null ? "toString()=" + super.toString() : "") + "]";
    }

}
