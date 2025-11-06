package ru.otus.hw.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
public class MessageProvider {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public MessageProvider(MessageSource messageSource, LocaleResolver localeResolver) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    public String getMessage(String code, Object... args) {
        Locale locale = localeResolver.resolveLocale(null);
        return messageSource.getMessage(code, args, locale);
    }

    public String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }
}
