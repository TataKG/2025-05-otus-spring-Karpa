package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.CategoryConverter;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.models.Category;
import ru.otus.hw.repositories.CategoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryConverter categoryConverter;
    private final MessageProvider messageProvider;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryConverter categoryConverter,
                               MessageProvider messageProvider) {
        this.categoryRepository = categoryRepository;
        this.categoryConverter = categoryConverter;
        this.messageProvider = messageProvider;
    }

    @Override
    public CategoryDto createCategory(String name) {
        if (categoryExists(name)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("category.already_exists", name)
            );
        }

        Category category = new Category(name);
        Category savedCategory = categoryRepository.save(category);
        return categoryConverter.toDto(savedCategory);
    }

    @Override
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryConverter::toDto);
    }

    @Override
    public Optional<CategoryDto> getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(categoryConverter::toDto);
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }
}
