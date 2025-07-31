package ru.otus.hw.mongock.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.ArrayList;
import java.util.List;

@ChangeLog
public class DatabaseChangelogTest {
    private final List<Author> authors = new ArrayList<>();

    private final List<Book> books = new ArrayList<>();

    private final List<Comment> comments = new ArrayList<>();

    private final List<Genre> genres = new ArrayList<>();

    @ChangeSet(order = "000", id = "dropDB", author = "owner_va", runAlways = true)
    public void dropDB(MongoDatabase database) {
        database.drop();
    }

    @ChangeSet(order = "001", id = "initAuthors", author = "owner_va", runAlways = true)
    public void initAuthors(AuthorRepository repository) {
        for (int i = 1; i <= 3; i++) {
            authors.add(new Author( String.valueOf(i),"Author_" + i));
        }
        repository.saveAll(authors);
    }

    @ChangeSet(order = "002", id = "initGenres", author = "owner_va", runAlways = true)
    public void initGenres(GenreRepository repository) {
        for (int i = 1; i <= 6; i++) {
            genres.add(new Genre(String.valueOf(i),"Genre_" + i));
        }
        repository.saveAll(genres);
    }

    @ChangeSet(order = "003", id = "initBooks", author = "owner_va", runAlways = true)
    public void initBooks(BookRepository repository) {

        books.add(new Book("Books_1", authors.get(0), genres.get(0)));
        books.add(new Book("Books_2", authors.get(1), genres.get(2)));
        books.add(new Book("Books_3", authors.get(1), genres.get(5)));

        repository.saveAll(books);
    }

    @ChangeSet(order = "004", id = "initComments", author = "owner_va", runAlways = true)
    public void initComments(CommentRepository repository) {
        for (int i = 1; i <= 4; i++) {
            comments.add(new Comment("Great book, really enjoyed it!_" + i, books.get(0).getId(), books.get(0)));
        }
        repository.saveAll(comments);
    }
}