package ru.otus.hw.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.otus.hw.models.Comment;

import javax.annotation.Nonnull;
import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    @Nonnull
    List<Comment> findByBookId(long bookId);
}
