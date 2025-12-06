package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Genre;

@RepositoryRestResource(path = "genres", collectionResourceRel = "genres")
public interface GenreRepository extends MongoRepository<Genre, String> {
}
