package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CsvQuestionDaoTest {
    @Mock
    TestFileNameProvider fileNameProvider;

    @InjectMocks
    CsvQuestionDao csvQuestionDao;

    @Test
    @DisplayName("Должен вернуть ошибку, если что-то не так с файлом при его чтении")
    void shouldReturnExceptionWhenFileRead() {
        given(fileNameProvider.getTestFileName()).willReturn(null);

        var exception = assertThrows(QuestionReadException.class, csvQuestionDao::findAll);

        assert (exception.getMessage().contains("Error Reading File"));
    }

    @Test
    @DisplayName("Должен корректно читать вопросы из файла")
    void shouldCorrectReadQuestions() {
        given(fileNameProvider.getTestFileName()).willReturn("questions.csv");
        var quiz = csvQuestionDao.findAll();

        assertThat(quiz).isNotNull();
        assertThat(quiz.size()).isEqualTo(3);

        //сравниваем вопросы и ответы
        var question = quiz.get(0);
        assertThat(question.text()).isEqualTo("Is there life on Mars?");
        assertThat(question.answers().size()).isEqualTo(3);
        assertThat(question.answers().get(0).text()).isEqualTo("Science doesn't know this yet");
        assertThat(question.answers().get(0).isCorrect()).isEqualTo(true);

        question = quiz.get(1);
        assertThat(question.text()).isEqualTo("How should resources be loaded form jar in Java?");
        assertThat(question.answers().size()).isEqualTo(3);
        assertThat(question.answers().get(0).text())
                .isEqualTo("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream");
        assertThat(question.answers().get(0).isCorrect()).isEqualTo(true);

        question = quiz.get(2);
        assertThat(question.text()).isEqualTo("Which option is a good way to handle the exception?");
        assertThat(question.answers().size()).isEqualTo(4);
        assertThat(question.answers().get(2).text())
                .isEqualTo("Rethrow with wrapping in business exception (for example, QuestionReadException)");
        assertThat(question.answers().get(2).isCorrect()).isEqualTo(true);
    }

}