package ru.otus.hw.service;

import ru.otus.hw.domain.Student;

public interface StudentService {
    void logIn(String firstName, String lastName);

    Student getCurrentStudent();

    void logOut();
}
