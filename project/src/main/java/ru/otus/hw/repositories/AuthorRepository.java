package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Long> {
    Optional<Author> findByUser(User user);

    Optional<Author> findByUserId(Long userId);

    @Query("SELECT a FROM Author a JOIN FETCH a.user u WHERE u.username = :username")
    Optional<Author> findByUserUsername(@Param("username") String username);

    @Query("SELECT a FROM Author a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Author> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT a FROM Author a JOIN FETCH a.user")
    List<Author> findAllWithUser();
}
