package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
// Настроить связи между объектами внутри модуля
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

// Добавить библиотеки для работы с потоками
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        // Использовать CsvToBean
        // https://opencsv.sourceforge.net/#collection_based_bean_fields_one_to_many_mappings
        // Использовать QuestionReadException
        // Про ресурсы: https://mkyong.com/java/java-read-a-file-from-resources-folder/

        ClassLoader loader = getClass().getClassLoader();
        InputStream resource = loader.getResourceAsStream(fileNameProvider.getTestFileName());

        // Проверить, что ресурс был сформирован
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
                    .withSkipLines(1)
                    .withSeparator(';')
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