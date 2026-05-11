package com.ciberaccion.booklibrary.book;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByGenreId(Long genreId);

    @Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b IN :books")
    List<Book> findBooksWithAuthors(@Param("books") List<Book> books);

    @Query("SELECT b FROM Book b JOIN FETCH b.genre WHERE b IN :books")
    List<Book> findBooksWithGenres(@Param("books") List<Book> books);

    @Query("""
            SELECT b FROM Book b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
            AND (:authorId IS NULL OR b.author.id = :authorId)
            AND (:genreId IS NULL OR b.genre.id = :genreId)
            """)
    List<Book> findWithFilters(
            @Param("title") String title,
            @Param("authorId") Long authorId,
            @Param("genreId") Long genreId);

}
