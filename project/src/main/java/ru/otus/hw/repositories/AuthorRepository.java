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

    @Query("SELECT a FROM Author a JOIN FETCH a.user WHERE a.user.id = :userId")
    Optional<Author> findByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Author a JOIN FETCH a.user u WHERE u.username = :username")
    Optional<Author> findByUserUsername(@Param("username") String username);

    @Query("SELECT a FROM Author a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Author> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM Author a " +
            "LEFT JOIN FETCH a.user u " +
            "ORDER BY a.createdAt DESC")
    List<Author> findAllWithUser();

    @Query("SELECT DISTINCT a FROM Author a " +
            "LEFT JOIN FETCH a.user u " +
            "LEFT JOIN FETCH u.roles " +
            "ORDER BY a.createdAt DESC")
    List<Author> findAllWithUserAndRoles();
}
