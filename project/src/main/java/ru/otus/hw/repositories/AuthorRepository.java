package ru.otus.hw.repositories;

import jakarta.persistence.NamedAttributeNode;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @EntityGraph(value = "Author.withUser", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Author> findByUser(User user);

    @EntityGraph(value = "Author.withUser", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT a FROM Author a WHERE a.user.id = :userId")
    Optional<Author> findByUserId(@Param("userId") Long userId);

    @EntityGraph(value = "Author.withUser", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT a FROM Author a WHERE a.user.username = :username")
    Optional<Author> findByUserUsername(@Param("username") String username);

    @EntityGraph(value = "Author.withUser", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Author> findById(Long id);

    @EntityGraph(value = "Author.withUser", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT a FROM Author a ORDER BY a.createdAt DESC")
    List<Author> findAllWithUser();

    @Query("SELECT a FROM Author a " +
            "LEFT JOIN FETCH a.user u " +
            "LEFT JOIN FETCH u.roles " +
            "WHERE a.id = :id")
    Optional<Author> findByIdWithUserAndRoles(@Param("id") Long id);

    @EntityGraph(value = "Author.withUser", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT a FROM Author a WHERE a.id = :id")
    Optional<Author> findByIdWithUser(@Param("id") Long id);
}
