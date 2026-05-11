package com.ciberaccion.booklibrary.genre;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    public Optional<Genre> findById(Long id) {
        return genreRepository.findById(id);
    }

    public Genre create(CreateGenreInput input) {
        Genre genre = Genre.builder()
                .name(input.name())
                .build();
        return genreRepository.save(genre);
    }
}
