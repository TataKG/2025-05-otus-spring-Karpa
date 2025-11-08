package ru.otus.hw.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
        try {
            // Получаем текущий запрос из контекста
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Locale locale = localeResolver.resolveLocale(request);
                return messageSource.getMessage(code, args, locale);
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not resolve locale from request, using default: " + e.getMessage());
        }

        // Fallback: используем локаль по умолчанию
        return messageSource.getMessage(code, args, Locale.getDefault());
    }

    public String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }
}
