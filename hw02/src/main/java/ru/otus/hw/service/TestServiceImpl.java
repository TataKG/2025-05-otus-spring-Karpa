package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private static final String QUESTION_FORMAT = "%2d. %s";

    private static final String ANSWER_FORMAT = "    %2d.%d) %s";

    private static final int MIN_ANSWER_INDEX = 1;

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        var testResult = new TestResult(student);

        try {
            var questions = questionDao.findAll();
            performQuiz(questions, testResult);
        } catch (QuestionReadException e) {
            ioService.printFormattedLine("Error reading test's question", e.getMessage());
            testResult.clearResults();
        }

        return testResult;
    }

    private void performQuiz(List<Question> quiestionList, TestResult testResult) {
        int questionIndex = 0;

        for (var question : quiestionList) {
            questionIndex++;
            writeQuestion(question, questionIndex);
            testResult.applyAnswer(question, analyseUserAnswer(question));
        }
    }

    private void writeQuestion(Question question, int questionIndex) {

        ioService.printFormattedLine(QUESTION_FORMAT, questionIndex, question.text());
        int answerIndex = MIN_ANSWER_INDEX;
        for (Answer answer : question.answers()) {
            ioService.printFormattedLine(ANSWER_FORMAT, questionIndex, answerIndex, answer.text());
            answerIndex++;
        }
    }

    private boolean analyseUserAnswer(Question question) {
        int userAnswer = ioService.readIntForRangeWithPrompt(
                MIN_ANSWER_INDEX,
                question.answers().size(),
                "Write your answer:",
                "It's not allowed answer"
        );
        return question.answers().get(userAnswer - MIN_ANSWER_INDEX).isCorrect();
    }

}
