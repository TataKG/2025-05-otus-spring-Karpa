package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CsvQuestionDao implements QuestionDao {

    private static final int COUNT_LINES_SKIP = 1;

    private static final char SEPARATOR = ';';

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        try (InputStream resource = getClass().getClassLoader().getResourceAsStream(
                fileNameProvider.getTestFileName())) {
            return readStreamToList(resource);
        } catch (IOException | RuntimeException e) {
            throw new QuestionReadException("Error Reading File", e);
        }
    }

    private List<Question> readStreamToList(InputStream resource) {
        try (var reader = new BufferedReader(new InputStreamReader(resource))) {

            CsvToBean<QuestionDto> csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withSeparator(SEPARATOR)
                    .withSkipLines(COUNT_LINES_SKIP)
                    .withType(QuestionDto.class)
                    .withOrderedResults(true)
                    .build();

            List<QuestionDto> questionsDto = csvToBean.stream().toList();
            if (questionsDto.isEmpty()) {
                throw new QuestionReadException("Question List is empty");
            }
            return questionsDto.stream().map(QuestionDto::toDomainObject).toList();

        } catch (RuntimeException | IOException e) {
            throw new QuestionReadException("Parsing error", e);
        }
    }

}
