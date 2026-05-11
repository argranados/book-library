package com.ciberaccion.booklibrary.book;

import java.time.LocalDate;

public record CreateBookInput(
        String title,
        String isbn,
        LocalDate publishedDate,
        String synopsis,
        Long authorId,
        Long genreId
) {}
