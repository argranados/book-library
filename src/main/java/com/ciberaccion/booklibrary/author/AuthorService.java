package com.ciberaccion.booklibrary.author;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    public Author create(CreateAuthorInput input) {
        Author author = Author.builder()
                .firstName(input.firstName())
                .lastName(input.lastName())
                .bio(input.bio())
                .build();
        return authorRepository.save(author);
    }

}
