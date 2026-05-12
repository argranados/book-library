package com.ciberaccion.booklibrary.author;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @QueryMapping
    public List<Author> authors() {
        return authorService.findAll();
    }

    @QueryMapping
    public Optional<Author> authorById(@Argument Long id) {
        return authorService.findById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @MutationMapping
    public Author createAuthor(@Argument CreateAuthorInput input) {
        return authorService.create(input);
    }
}
