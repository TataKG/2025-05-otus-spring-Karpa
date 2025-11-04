package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import ru.otus.hw.models.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    @EntityGraph("user-roles-graph")
    Optional<User> findByUsername(String username);
}
