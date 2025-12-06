package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.Bread;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BreadConverter {

    public String formatBreads(List<Bread> breads) {
        if (breads == null || breads.isEmpty()) {
            return "Нет хлеба";
        }

        return breads.stream()
                .map(bread -> bread.type() + "(" + bread.quantity() + ")")
                .collect(Collectors.joining(", "));
    }
}