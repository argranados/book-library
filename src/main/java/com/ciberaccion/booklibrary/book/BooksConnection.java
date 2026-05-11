package com.ciberaccion.booklibrary.book;

import java.util.List;

public record BooksConnection(
        List<BookEdge> edges,
        PageInfo pageInfo
) {
    public record BookEdge(Book node, String cursor) {}

    public record PageInfo(
            boolean hasNextPage,
            boolean hasPreviousPage,
            String startCursor,
            String endCursor
    ) {}
}