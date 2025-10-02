package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final AuthorDtoConverter authorDtoConverter;

    @Override
    public Flux<AuthorDto> findAll() {
        return authorRepository.findAll()
                .map(authorDtoConverter::toDto);
    }

    @Override
    public Mono<AuthorDto> findById(String id) {
        return authorRepository.findById(id).map(authorDtoConverter::toDto);
    }

}
