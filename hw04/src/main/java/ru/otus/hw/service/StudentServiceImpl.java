package ru.otus.hw.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Student;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final LocalizedIOService ioService;

    @Getter
    private Student currentStudent;

    @Override
    public void logIn(String firstName, String lastName) {
      currentStudent = new Student(firstName, lastName);
    }

    public void logOut() {
        currentStudent = null;
    }
}
