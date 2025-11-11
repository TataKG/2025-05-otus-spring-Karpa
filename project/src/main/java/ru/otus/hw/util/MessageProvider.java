package ru.otus.hw.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class MessageProvider {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public String getMessage(String code, Object... args) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Locale locale = localeResolver.resolveLocale(request);
                return messageSource.getMessage(code, args, locale);
            }
        } catch (Exception e) {
        }

        return messageSource.getMessage(code, args, Locale.getDefault());
    }

    public String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }
}