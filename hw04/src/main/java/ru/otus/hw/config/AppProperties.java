package ru.otus.hw.config;

import lombok.Data;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Locale;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "application")
public class AppProperties implements TestConfig, TestFileNameProvider, LocaleConfig {
    @Getter
    private int rightAnswersCountToPass;
    @Getter
    private Locale locale;

    private Map<String, String> fileNameByLocaleTag;

    public AppProperties(
            @Value("${application.rightAnswersCountToPass}") int rightAnswersCountToPass,
            @Value("${application.locale}") String locale,
            @Value("${application.fileNameByLocaleTag}") Map<String, String> fileNameByLocaleTag) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.locale = Locale.forLanguageTag(locale);
        this.fileNameByLocaleTag = fileNameByLocaleTag;
    }

    @Override
    public String getTestFileName() {

        return fileNameByLocaleTag.get(locale.toLanguageTag());
    }
}

