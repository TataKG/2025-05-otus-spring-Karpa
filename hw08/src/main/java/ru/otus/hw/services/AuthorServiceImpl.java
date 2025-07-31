package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final AuthorDtoConverter authorDtoConverter;

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream()
                .map(authorDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AuthorDto> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return authorRepository.findById(id).map(authorDtoConverter::toDto);
    }

}
