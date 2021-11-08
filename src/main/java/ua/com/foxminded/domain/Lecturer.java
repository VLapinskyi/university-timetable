package ua.com.foxminded.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("Lecturer")
public class Lecturer extends Person {    

    public Lecturer() {

    }

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
