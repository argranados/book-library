package com.ciberaccion.booklibrary.book;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.ciberaccion.booklibrary.author.Author;
import com.ciberaccion.booklibrary.genre.Genre;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @QueryMapping
    public List<Book> books() {
        return bookService.findAll();
    }

    @QueryMapping
    public Optional<Book> bookById(@Argument Long id) {
        return bookService.findById(id);
    }

    @QueryMapping
    public List<Book> booksByGenre(@Argument Long genreId) {
        return bookService.findByGenreId(genreId);
    }

    @MutationMapping
    public Book createBook(@Argument CreateBookInput input) {
        return bookService.create(input);
    }

    @MutationMapping
    public boolean deleteBook(@Argument Long id) {
        return bookService.delete(id);
    }

    // ---- BATCH MAPPING ----

    @BatchMapping
    public Map<Book, Author> author(List<Book> books) {
        return bookService.findBooksWithAuthors(books)
                .stream()
                .collect(Collectors.toMap(
                        book -> book,
                        Book::getAuthor));
    }

    @BatchMapping
    public Map<Book, Genre> genre(List<Book> books) {
        return bookService.findBooksWithGenres(books)
                .stream()
                .collect(Collectors.toMap(
                        book -> book,
                        Book::getGenre));
    }
}
