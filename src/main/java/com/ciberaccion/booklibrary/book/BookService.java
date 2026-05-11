package com.ciberaccion.booklibrary.book;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ciberaccion.booklibrary.author.Author;
import com.ciberaccion.booklibrary.author.AuthorRepository;
import com.ciberaccion.booklibrary.exception.AuthorNotFoundException;
import com.ciberaccion.booklibrary.genre.Genre;
import com.ciberaccion.booklibrary.genre.GenreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        // No lanzamos aquí — el controller decide si es error o null válido
        return bookRepository.findById(id);
    }

    public List<Book> findByGenreId(Long genreId) {
        return bookRepository.findByGenreId(genreId);
    }

    public Book create(CreateBookInput input) {
        Author author = authorRepository.findById(input.authorId())
                // Después (específico, con clasificación):
                .orElseThrow(() -> new AuthorNotFoundException(input.authorId()));

        Genre genre = genreRepository.findById(input.genreId())
                .orElseThrow(() -> new RuntimeException("Genre not found: " + input.genreId()));

        Book book = Book.builder()
                .title(input.title())
                .isbn(input.isbn())
                .publishedDate(input.publishedDate())
                .synopsis(input.synopsis())
                .author(author)
                .genre(genre)
                .build();

        return bookRepository.save(book);
    }

    public boolean delete(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }
        bookRepository.deleteById(id);
        return true;
    }

    public List<Book> findBooksWithAuthors(List<Book> books) {
        return bookRepository.findBooksWithAuthors(books);
    }

    public List<Book> findBooksWithGenres(List<Book> books) {
        return bookRepository.findBooksWithGenres(books);
    }

    public List<Book> findWithFilters(BookFilterInput filter) {
        if (filter == null) {
            return bookRepository.findAll();
        }
        return bookRepository.findWithFilters(
                filter.title(),
                filter.authorId(),
                filter.genreId());
    }
}
