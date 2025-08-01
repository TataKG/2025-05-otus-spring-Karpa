package ru.otus.hw.services.mongo.listeners;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.CommentRepository;

@Component
@RequiredArgsConstructor
public class BookCascadeDeleteListener extends AbstractMongoEventListener<Book> {
    private final CommentRepository commentRepository;

    @Override
    public void onBeforeDelete(@NonNull BeforeDeleteEvent<Book> event) {
        super.onBeforeDelete(event);
        String bookId = event.getSource().get("_id").toString();
        commentRepository.deleteAllByBookId(bookId);
    }
}
