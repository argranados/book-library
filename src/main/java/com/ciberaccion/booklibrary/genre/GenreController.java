package com.ciberaccion.booklibrary.genre;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @QueryMapping
    public List<Genre> genres() {
        return genreService.findAll();
    }

    @MutationMapping
    public Genre createGenre(@Argument CreateGenreInput input) {
        return genreService.create(input);
    }
}
