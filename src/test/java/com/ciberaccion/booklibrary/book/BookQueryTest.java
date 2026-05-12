package com.ciberaccion.booklibrary.book;

import com.ciberaccion.booklibrary.author.Author;
import com.ciberaccion.booklibrary.config.GraphQLConfig;
import com.ciberaccion.booklibrary.genre.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(BookController.class)
@Import(GraphQLConfig.class)
class BookQueryTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    BookService bookService;

    @Test
    void books_retornaLista() {
        Author author = Author.builder()
                .id(1L).firstName("Robert").lastName("Martin").build();
        Genre genre = Genre.builder()
                .id(1L).name("Programming").build();
        Book book = Book.builder()
                .id(1L).title("Clean Code").isbn("978-0132350884")
                .author(author).genre(genre).build();

        when(bookService.findWithFilters(any())).thenReturn(List.of(book));
        when(bookService.findBooksWithAuthors(any())).thenReturn(List.of(book));
        when(bookService.findBooksWithGenres(any())).thenReturn(List.of(book));

        graphQlTester.document("""
                query {
                    books {
                        id
                        title
                        isbn
                    }
                }
                """)
                .execute()
                .path("books[0].title")
                .entity(String.class)
                .isEqualTo("Clean Code");
    }
}