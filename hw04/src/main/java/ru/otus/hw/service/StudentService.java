package ru.otus.hw.service;

import ru.otus.hw.domain.Student;

public interface StudentService {
    void logIn();

    Student getCurrentStudent();

    void logOut();
}
