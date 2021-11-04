INSERT INTO faculties (name) VALUES ('TestFaculty1');
INSERT INTO faculties (name) VALUES ('TestFaculty2');
INSERT INTO faculties (name) VALUES ('TestFaculty3');

INSERT INTO groups (name, faculty_id) VALUES ('TestGroup1', 1);
INSERT INTO groups (name, faculty_id) VALUES ('TestGroup2', 2);
INSERT INTO groups (name, faculty_id) VALUES ('TestGroup3', 1);

INSERT INTO lesson_times (start_time, end_time) VALUES ('09:00:00', '10:30:00');
INSERT INTO lesson_times (start_time, end_time) VALUES ('10:45:00', '12:15:00');
INSERT INTO lesson_times (start_time, end_time) VALUES ('12:30:00', '14:00:00');
    
-- Students
INSERT INTO people (role, first_name, last_name, gender, phone_number, email, student_group_id)
    VALUES ('Student', 'Daria', 'Hrynchuk', 'FEMALE', '+380992222222', 'd.hrynchuk@gmail.com', 1);
INSERT INTO people (role, first_name, last_name, gender, phone_number, email, student_group_id)
    VALUES ('Student', 'Illia', 'Misiats', 'MALE', '+380323659872', 'illiamisiats@gmail.com', 1);
INSERT INTO people (role, first_name, last_name, gender, phone_number, email, student_group_id)
    VALUES ('Student', 'Mykhailo', 'Mazur', 'MALE', '+380327485963', 'm.mazur@test.com', 2);

-- Lecturers
INSERT INTO people (role, first_name, last_name, gender, phone_number, email)
    VALUES ('Lecturer', 'Olena', 'Skladenko', 'FEMALE', '+380991111111', 'oskladenko@gmail.com');
INSERT INTO people (role, first_name, last_name, gender, phone_number, email)
    VALUES ('Lecturer', 'Ihor', 'Zakharchuk', 'MALE', '+380125263741', 'i.zakharchuk@gmail.com');
INSERT INTO people (role, first_name, last_name, gender, phone_number, email)
    VALUES ('Lecturer', 'Vasyl', 'Dudchenko', 'MALE', '+380457895263', 'vdudchenko@test.com');
    
INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Ukranian', 4, 1, '101', 'SUNDAY', 1);
INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Music', 5, 3, '102', 'TUESDAY', 1);
INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Physical Exercises', 6, 1, '103', 'WEDNESDAY', 3);
INSERT INTO lessons (name, lecturer_id, group_id, audience, week_day, lesson_time_id)
    VALUES ('Physical Exercises', 6, 2, '103', 'WEDNESDAY', 2);