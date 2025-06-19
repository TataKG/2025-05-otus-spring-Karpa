package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;


@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {
    private static final String QUESTION_FORMAT = "%2d. %s";

    private static final String ANSWER_FORMAT = "    %2d.%c) %s";

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @Test
    @DisplayName("Должен выдавать ошибку при null вопросах")
    void shouldThrowExceptionWhenNullQuestions() {

        final String NO_QUESTIONS_FOUND = "List is null";

        given(questionDao.findAll()).willReturn(null);

        var exception = assertThrows(RuntimeException.class, () -> testService.executeTest());

        assertTrue(exception.getMessage().contains(NO_QUESTIONS_FOUND));
    }

    @Test
    @DisplayName("Проверка корректности вопросов и ответов")
    void shouldExecuteTest(){

       final String quizQuestion1 = "What's the most popular fruit?";
       final String quizQuestion2 = "What's the most popular vegetable?";
       final String answer1_a = "Banan";
       final String answer1_b = "Apple";
       final String answer1_3 = "Orange";
       final String answer2_1 = "Tomato";
       final String answer2_2 = "Potato";
       final String answer2_3 = "Onion";

       List<Question> questions = List.of(
               new Question( quizQuestion1,
                       List.of( new Answer(answer1_a, false),
                                new Answer(answer1_b, true),
                                new Answer(answer1_3, false)
                       )),
               new Question(quizQuestion2,
                       List.of( new Answer(answer2_1,true),
                                new Answer(answer2_2, false),
                                new Answer(answer2_3, false)
                       ))

       );

       given(questionDao.findAll()).willReturn(questions);

       testService.executeTest();

       // Проверяем, что вызвался findAll
       verify(questionDao,times(1)).findAll();

       // Проверяем ответы от сервиса
       verify(ioService,times(1))
               .printFormattedLine("Please answer the questions below%n");

       verify(ioService,times(1))
               .printFormattedLine(QUESTION_FORMAT, 1, quizQuestion1);

       verify(ioService,times(1))
               .printFormattedLine(ANSWER_FORMAT, 1, 'a', answer1_a);

       verify(ioService,times(1))
                .printFormattedLine(ANSWER_FORMAT, 1, 'b', answer1_b);

       verify(ioService,times(1))
                .printFormattedLine(ANSWER_FORMAT, 1, 'c', answer1_3);

       verify(ioService,times(1))
                .printFormattedLine(QUESTION_FORMAT, 2, quizQuestion2);

       verify(ioService,times(1))
                .printFormattedLine(ANSWER_FORMAT, 2, 'a', answer2_1);

       verify(ioService,times(1))
                .printFormattedLine(ANSWER_FORMAT, 2, 'b', answer2_2);

       verify(ioService,times(1))
                .printFormattedLine(ANSWER_FORMAT, 2, 'c', answer2_3);
    }

}