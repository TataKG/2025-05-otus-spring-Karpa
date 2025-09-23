package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest(classes = {TestServiceImpl.class})
@DisplayName("Сервис выполнения тестирования")
class TestServiceImplTest {

    private static final String EXPECTED_QUESTION_FORMAT = "%2d. %s";

    private static final String EXPECTED_ANSWER_FORMAT = "    %2d.%d) %s";

    @MockitoBean
    private LocalizedIOService ioService;

    @MockitoBean
    private QuestionDao questionDao;

    @Autowired
    private TestServiceImpl testService;

    @Test
    @DisplayName("Должен вернуть результат выполнения теста с корректным количеством правильных ответов")
    void ShouldReturnTestResultWithCorrectAnswers() {
        List<Question> testQuestions = List.of(
                new Question("What's the most popular fruit?",
                        List.of(
                                new Answer("Banan", false),
                                new Answer("Apple", true),
                                new Answer("Tomato", false)
                        )),
                new Question("What's the most popular vegetable?",
                        List.of(
                                new Answer("Tomato", false),
                                new Answer("Potato", false),
                                new Answer("Onion", true))
                )
        );

        //Настраиваем, что возвращает DAO для вопросов
        given(questionDao.findAll()).willReturn(testQuestions);

        //Задаем ответы пользователя
        given(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(2)
                .willReturn(3);

        //Сравнение
        var resultTest = testService.executeTestFor(new Student("Tata", "JK"));

        assertThat(resultTest).isNotNull();
        assertThat(resultTest.getRightAnswersCount()).isEqualTo(2);

        //Проверка, что API вызывается нужное количество раз
        verify(questionDao, times(1)).findAll();
        verify(ioService, times(2))
                .readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString());

    }

    @Test
    @DisplayName("Проверка корректности вопросов и ответов")
    void shouldCorrectQuizFormat() {
        List<Question> testQuestions = List.of(
                new Question("Question1",
                        List.of(new Answer("answer1", false),
                                new Answer("answer2", true),
                                new Answer("answer3", false)
                        )),
                new Question("Question2",
                        List.of(new Answer("answer1", true),
                                new Answer("answer2", false),
                                new Answer("answer3", false)
                        ))

        );

        //Настраиваем, что возвращает DAO для вопросов
        given(questionDao.findAll()).willReturn(testQuestions);

        // Симулируем ввод ответов пользователем
        given(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(2)
                .willReturn(1);

        //Сравнение
        testService.executeTestFor(new Student("Tata", "JK"));

        //Проверяем, что вызвался API нужно число раз
        verify(questionDao, times(1)).findAll();

        //Проверяем выданное сервисом
        //Вступительная фраза
        verify(ioService, times(1))
                .printLineLocalized("TestService.answer.the.questions");

        verify(ioService, times(1))
                .printFormattedLine(EXPECTED_QUESTION_FORMAT, 1, "Question1");

        verify(ioService, times(1))
                .printFormattedLine(EXPECTED_ANSWER_FORMAT, 1, 1, "answer1");

        verify(ioService, times(1))
                .printFormattedLine(EXPECTED_ANSWER_FORMAT, 1, 2, "answer2");

        verify(ioService, times(1))
                .printFormattedLine(EXPECTED_ANSWER_FORMAT, 1, 3, "answer3");

    }

}