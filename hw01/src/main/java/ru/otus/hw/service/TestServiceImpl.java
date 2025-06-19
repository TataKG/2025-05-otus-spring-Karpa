package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private static final String QUESTION_FORMAT = "%2d. %s";

    private static final String ANSWER_FORMAT = "    %2d.%c) %s";

    private static final char START_ANSWER_INDEX = 'a';

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        List<Question> questionList = questionDao.findAll();
        if (questionList == null) {
            throw new RuntimeException("List is null");
        }
        startTest(questionList);
    }

    private void startTest(List<Question> questionList) {
        int questionIndex = 0;
        char answerIndex;

        for (Question question : questionList) {
            questionIndex++;
            ioService.printFormattedLine(QUESTION_FORMAT, questionIndex, question.text());

            answerIndex = START_ANSWER_INDEX;
            for (Answer answer: question.answers()) {
                ioService.printFormattedLine(ANSWER_FORMAT,questionIndex, answerIndex, answer.text());
                answerIndex++;
            }
        }
    }
}
