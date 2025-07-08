package ru.otus.hw.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "test")
public class AppProperties implements TestConfig, TestFileNameProvider, LocaleConfig {

    private final int rightAnswersCountToPass;

    private Locale locale;

    private Map<String, String> fileNameByLocaleTag;

    public AppProperties(
            @Value("${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
            @Value("${test.locale}") String locale,
            @Value("${test.fileNameByLocaleTag}") Map<String, String> fileNameByLocaleTag) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.locale = Locale.forLanguageTag(locale);
        this.fileNameByLocaleTag = fileNameByLocaleTag;
    }

    @Override
    public String getTestFileName() {

        return fileNameByLocaleTag.get(locale.toLanguageTag());
    }
}
