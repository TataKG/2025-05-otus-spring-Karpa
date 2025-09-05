package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

@DataMongoTest
public abstract class BaseMongoTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }

    protected Author createAuthor(String fullName) {
        Author author = new Author(fullName);
        return mongoTemplate.save(author);
    }

    protected Genre createGenre(String name) {
        Genre genre = new Genre(name);
        return mongoTemplate.save(genre);
    }

    protected Book createBook(String title, Author author, Genre genre) {
        Book book = new Book(title, author, genre);
        return mongoTemplate.save(book);
    }

    protected Comment createComment(String text, String bookId) {
        Comment comment = new Comment(text, bookId);
        return mongoTemplate.save(comment);
    }
}