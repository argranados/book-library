package com.ciberaccion.booklibrary.book;

public record BookFilterInput(
        String title,
        Long authorId,
        Long genreId
) {}