DROP TABLE IF EXISTS
    people,
    groups,
    faculties,
    lessons,
    lesson_times
    CASCADE;
DROP TYPE IF EXISTS
    gender CASCADE;
DROP TYPE IF EXISTS
    week_day CASCADE;
DROP TYPE IF EXISTS
    role CASCADE;
    
CREATE TYPE gender AS ENUM ('MALE', 'FEMALE');
CREATE TYPE week_day AS ENUM ('SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY');
CREATE TYPE role AS ENUM ('STUDENT', 'LECTURER');

CREATE TABLE faculties (
    id serial NOT NULL,
    name character varying NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE groups (
    id serial NOT NULL,
    name character varying NOT NULL,
    faculty_id integer,
    PRIMARY KEY (id),
    FOREIGN KEY (faculty_id) REFERENCES public.faculties (id) ON DELETE CASCADE,
    UNIQUE (name)
);

CREATE TABLE people (
    id serial NOT NULL,
    role role NOT NULL,
    first_name character varying NOT NULL,
    last_name character varying NOT NULL,
    gender gender NOT NULL,
    phone_number character varying,
    email character varying,
    student_group_id integer,
    PRIMARY KEY (id),
    FOREIGN KEY (student_group_id) REFERENCES public.groups (id) ON DELETE CASCADE,
    UNIQUE (phone_number),
    UNIQUE (email)
);

CREATE TABLE lesson_times (
    id serial NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_time, end_time)
);

CREATE TABLE lessons (
    id serial NOT NULL,
    name character varying NOT NULL,
    lecturer_id integer,
    group_id integer,
    audience character varying NOT NULL,
    week_day week_day NOT NUll,
    lesson_time_id integer,
    PRIMARY KEY (id),
    FOREIGN KEY (lecturer_id) REFERENCES public.people (id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES public.groups (id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_time_id) REFERENCES public.lesson_times (id) ON DELETE CASCADE
);