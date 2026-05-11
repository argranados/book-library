package com.ciberaccion.booklibrary.author;

public record CreateAuthorInput(
        String firstName,
        String lastName,
        String bio) {
}
