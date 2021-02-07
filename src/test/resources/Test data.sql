INSERT INTO faculties (name) VALUES ('TestFaculty1');
INSERT INTO faculties (name) VALUES ('TestFaculty2');
INSERT INTO faculties (name) VALUES ('TestFaculty3');

INSERT INTO groups (name, faculty_id) VALUES ('TestGroup1', 1);
INSERT INTO groups (name, faculty_id) VALUES ('TestGroup2', 2);
INSERT INTO groups (name, faculty_id) VALUES ('TestGroup3', 1);

INSERT INTO lesson_times (start_time, end_time) VALUES ('09:00:00', '10:30:00');
INSERT INTO lesson_times (start_time, end_time) VALUES ('10:45:00', '12:15:00');
INSERT INTO lesson_times (start_time, end_time) VALUES ('12:30:00', '14:00:00');

-- Lecturers
INSERT INTO people (first_name, last_name, gender, phone_number, email)
    VALUES ('Olena', 'Skladenko', 'female', '+380991111111', 'oskladenko@gmail.com');
INSERT INTO people (first_name, last_name, gender, phone_number, email)
    VALUES ('Ihor', 'Zakharchuk', 'male', null, 'i.zakharchuk@gmail.com');
INSERT INTO people (first_name, last_name, gender, phone_number, email)
    VALUES ('Vasyl', 'Dudchenko', 'male', null, null);
    
-- Students
INSERT INTO people (first_name, last_name, gender, phone_number, email, student_group_id)
    VALUES ('Daria', 'Hrynchuk', 'female', '+380992222222', 'd.hrynchuk@gmail.com', 1);
INSERT INTO people (first_name, last_name, gender, phone_number, email, student_group_id)
    VALUES ('Illia', 'Misiats', 'male', null, 'illiamisiats@gmail.com', 1);
INSERT INTO people (first_name, last_name, gender, phone_number, email, student_group_id)
    VALUES ('Mykhailo', 'Mazur', 'male', null, null, 2);

INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Ukranian', 1, 1, '101', 1, 1);
INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Music', 2, 3, '102', 3, 1);
INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Physical Exercises', 3, 1, '103', 4, 3);