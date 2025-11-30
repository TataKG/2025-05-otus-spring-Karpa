package ru.otus.hw.mongo.changelog.mongock;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@ChangeUnit(id = "database-initializer", order = "001", author = "owner_va")
@RequiredArgsConstructor
public class DatabaseChangelog {

    @Execution
    public void initializeDatabase(
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            BookRepository bookRepository,
            CommentRepository commentRepository,
            UserRepository userRepository) {

        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
        userRepository.deleteAll();

        List<Author> authors = initAuthors(authorRepository);

        List<Genre> genres = initGenres(genreRepository);

        List<Book> books = initBooks(bookRepository, authors, genres);

        initComments(commentRepository, books);

        initUsers(userRepository);
    }

    private List<Author> initAuthors(AuthorRepository repository) {
        List<Author> authors = new ArrayList<>();
        authors.add(new Author("Charlotte Bronte"));
        authors.add(new Author("Agatha Christie"));
        authors.add(new Author("Charles Dickens"));

        return repository.saveAll(authors);
    }

    private List<Genre> initGenres(GenreRepository repository) {
        List<Genre> genres = new ArrayList<>();
        genres.add(new Genre("Romance"));
        genres.add(new Genre("Mystery"));
        genres.add(new Genre("Thriller"));

        return repository.saveAll(genres);
    }

    private List<Book> initBooks(BookRepository repository, List<Author> authors, List<Genre> genres) {
        List<Book> books = new ArrayList<>();

        if (!authors.isEmpty() && !genres.isEmpty()) {
            books.add(new Book("Jane Eyre", authors.get(0), genres.get(0)));
            books.add(new Book("Shirley", authors.get(0), genres.get(0)));
            books.add(new Book("Murder on the Orient Express", authors.get(1), genres.get(2)));
            books.add(new Book("Death on the Nile", authors.get(1), genres.get(2)));
            books.add(new Book("Oliver Twist", authors.get(2), genres.get(0)));
            books.add(new Book("David Copperfield", authors.get(2), genres.get(1)));

            return repository.saveAll(books);
        }
        return books;
    }

    private void initComments(CommentRepository repository, List<Book> books) {
        List<Comment> comments = new ArrayList<>();

        if (!books.isEmpty()) {
            Book firstBook = books.get(0);
            for (int i = 1; i <= 4; i++) {
                comments.add(new Comment("Great book, really enjoyed it!_" + i, firstBook.getId()));
            }
            repository.saveAll(comments);
        }
    }

    private void initUsers(UserRepository userRepository) {
        User admin = new User();
        admin.setName("admin");
        admin.setPassword("$2a$12$2GEwA0dLBzLH0WfH3CvbuOnopW8ovvLV1QXAC2PYrY8Iqilm5zeey"); // admin
        admin.setRoles(List.of("ADMIN", "USER", "MONITOR"));

        User user = new User();
        user.setName("user");
        user.setPassword("$2a$12$PFmuXFu0qUlQMcpDWGRmPOeQnZ1OiKiumfhXISwxcgBErP0kXzKoy"); // user
        user.setRoles(List.of("USER"));

        User monitor = new User();
        monitor.setName("monitor");
        monitor.setPassword("$2a$12$PFmuXFu0qUlQMcpDWGRmPOeQnZ1OiKiumfhXISwxcgBErP0kXzKoy"); // user (same password)
        monitor.setRoles(List.of("USER", "MONITOR"));

        userRepository.saveAll(List.of(admin, user, monitor));
    }

    @RollbackExecution
    public void rollback(
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            BookRepository bookRepository,
            CommentRepository commentRepository,
            UserRepository userRepository) {

        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
        userRepository.deleteAll();
    }
}