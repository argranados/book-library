package com.ciberaccion.booklibrary.book;

import com.ciberaccion.booklibrary.config.GraphQLConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(BookController.class)
@Import({ GraphQLConfig.class, BookMutationSecurityTest.SecurityTestConfig.class })
class BookMutationSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class SecurityTestConfig {
    }

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    BookService bookService;

    @Test
    void createBook_sinAutenticacion_retornaError() {
        graphQlTester.document("""
                mutation {
                    createBook(input: {
                        title: "Clean Code"
                        isbn: "978-0132350884"
                        authorId: "1"
                        genreId: "1"
                    }) {
                        id
                        title
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assert !errors.isEmpty();
                    System.out.println(">>> Error sin auth: " + errors.get(0).getMessage());
                });
    }
}