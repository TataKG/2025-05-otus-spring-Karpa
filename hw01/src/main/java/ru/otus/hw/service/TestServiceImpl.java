package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        // Получить вопросы из дао и вывести их с вариантами ответ
        List<Question> questionList = questionDao.findAll();
        // Провести тестирование
        startTest(questionList);
    }

    private void startTest(List<Question> questionList) {
        int questionIndex = 0;
        int answerIndex;

        for (Question question : questionList) {
            questionIndex++;
            // Текст вопроса
            ioService.printLine(questionIndex + ". " + question.text());
            // Варианты ответов
            answerIndex = 0;
            for (Answer answer: question.answers()) {
                answerIndex++;
                ioService.printLine("  " + questionIndex + "." + answerIndex + ". " + answer.text());
              }
        }
    }
}
