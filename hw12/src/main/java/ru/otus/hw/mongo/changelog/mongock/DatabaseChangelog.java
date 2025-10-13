package ru.otus.hw.mongo.changelog.mongock;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.models.*;
import ru.otus.hw.repositories.*;

import java.util.ArrayList;
import java.util.List;

@ChangeLog
public class DatabaseChangelog {
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

        authors.add(new Author("Charlotte Bronte"));
        authors.add(new Author("Agatha Christie"));
        authors.add(new Author("Charles Dickens"));

        repository.saveAll(authors);
    }

    @ChangeSet(order = "002", id = "initGenres", author = "owner_va", runAlways = true)
    public void initGenres(GenreRepository repository) {
        genres.add(new Genre("Romance"));
        genres.add(new Genre("Mystery"));
        genres.add(new Genre("Thriller"));

        repository.saveAll(genres);
    }

    @ChangeSet(order = "003", id = "initBooks", author = "owner_va", runAlways = true)
    public void initBooks(BookRepository repository) {

        books.add(new Book("Jane Eyre", authors.get(0), genres.get(0)));
        books.add(new Book("Shirley", authors.get(0), genres.get(0)));
        books.add(new Book("Murder on the Orient Express", authors.get(1), genres.get(2)));
        books.add(new Book("Death on the Nile", authors.get(1), genres.get(2)));
        books.add(new Book("Oliver Twist", authors.get(2), genres.get(0)));
        books.add(new Book("David Copperfield", authors.get(2), genres.get(1)));

        repository.saveAll(books);
    }

    @ChangeSet(order = "004", id = "initComments", author = "owner_va", runAlways = true)
    public void initComments(CommentRepository repository) {
        for (int i = 1; i <= 4; i++) {
            comments.add(new Comment("Great book, really enjoyed it!_" + i, books.get(0).getId()));
        }
        repository.saveAll(comments);
    }

    @ChangeSet(order = "005", id = "initUsers", author = "owner_va", runAlways = true)
    public void initUsers(UserRepository userRepository) {
        User admin = new User();
        admin.setName("admin");
        admin.setPassword("$2a$12$2GEwA0dLBzLH0WfH3CvbuOnopW8ovvLV1QXAC2PYrY8Iqilm5zeey"); //admin
        admin.setRoles(List.of("ADMIN", "USER"));

        User user = new User();
        user.setName("user");
        user.setPassword("$2a$12$PFmuXFu0qUlQMcpDWGRmPOeQnZ1OiKiumfhXISwxcgBErP0kXzKoy"); //user
        user.setRoles(List.of("USER"));

        userRepository.saveAll(List.of(admin, user));
    }
}
