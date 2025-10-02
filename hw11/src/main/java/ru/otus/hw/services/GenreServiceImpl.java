package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.GenreRepository;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    private final GenreDtoConverter genreDtoConverter;

    @Override
    public Flux<GenreDto> findAll() {
        return genreRepository.findAll()
                .map(genreDtoConverter::toDto);
    }

    @Override
    public Mono<GenreDto> findById(String id) {
        return genreRepository.findById(id).map(genreDtoConverter::toDto);
    }
}
