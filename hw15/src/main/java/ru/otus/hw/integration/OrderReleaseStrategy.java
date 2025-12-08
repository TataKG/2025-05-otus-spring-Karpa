package ru.otus.hw.integration;

import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.stereotype.Component;

@Component
public class OrderReleaseStrategy implements ReleaseStrategy {

    @Override
    public boolean canRelease(MessageGroup group) {
        Integer sequenceSize = group.getOne().getHeaders()
                .get("sequenceSize", Integer.class);

        if (sequenceSize == null) {
            return false;
        }

        int currentSize = group.size();
        boolean shouldRelease = currentSize >= sequenceSize;

        if (shouldRelease) {
            System.out.println("Агрегация завершена для группы: " +
                    currentSize + " из " + sequenceSize + " элементов");
        }

        return shouldRelease;
    }
}
