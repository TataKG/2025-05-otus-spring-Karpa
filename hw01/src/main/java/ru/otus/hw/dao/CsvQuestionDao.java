package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;

import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {

    private static final int COUNT_LINES_SKIP = 1;

    private static final char SEPARATOR = ';';

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {

        ClassLoader loader = getClass().getClassLoader();
        InputStream resource = loader.getResourceAsStream(fileNameProvider.getTestFileName());

        if (resource == null) {
            throw new QuestionReadException(
                    String.format("File access error - \"%s\"", fileNameProvider.getTestFileName()));
        }

        return readStreamToList(resource);
    }

    private List<Question> readStreamToList(InputStream resource) {
        try {
            List<QuestionDto> questionsDto = new CsvToBeanBuilder<QuestionDto>(
                    new InputStreamReader(resource))
                    .withSkipLines(COUNT_LINES_SKIP)
                    .withSeparator(SEPARATOR)
                    .withType(QuestionDto.class)
                    .build().parse();

            return questionsDto.stream()
                    .map(QuestionDto::toDomainObject)
                    .collect(Collectors.toList());

        } catch (RuntimeException e) {
            throw new QuestionReadException("File reading error", e);
        }
    }
}