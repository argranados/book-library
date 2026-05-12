package com.ciberaccion.booklibrary.author;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ciberaccion.booklibrary.config.GraphQLConfig;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

import com.ciberaccion.booklibrary.config.GraphQLConfig;  // ← importa tu config

@GraphQlTest(AuthorController.class)
@Import(GraphQLConfig.class)                              // ← esta línea es la solución
class AuthorControllerTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    AuthorService authorService;

    @Test
    void authors_returnsListOfAuthors() {
        // Arrange — qué devuelve el mock
        when(authorService.findAll()).thenReturn(List.of(
            Author.builder()
                .id(1L)
                .firstName("Robert")
                .lastName("Martin")
                .bio("Author of Clean Code")
                .build()
        ));

        // Act + Assert
        graphQlTester.document("""
            query {
                authors {
                    id
                    firstName
                    lastName
                }
            }
            """)
            .execute()
            .path("authors")
            .entityList(Author.class)
            .hasSize(1);
    }

    @Test
    void authorById_whenExists_returnsAuthor() {
        when(authorService.findById(1L)).thenReturn(
            Optional.of(Author.builder()
                .id(1L)
                .firstName("Robert")
                .lastName("Martin")
                .build())
        );

        graphQlTester.document("""
            query {
                authorById(id: "1") {
                    id
                    firstName
                }
            }
            """)
            .execute()
            .path("authorById.firstName")
            .entity(String.class)
            .isEqualTo("Robert");
    }

    @Test
    void authorById_whenNotExists_returnsNull() {
        when(authorService.findById(99L)).thenReturn(Optional.empty());

        graphQlTester.document("""
            query {
                authorById(id: "99") {
                    id
                    firstName
                }
            }
            """)
            .execute()
            .path("authorById")
            .valueIsNull();
    }
}