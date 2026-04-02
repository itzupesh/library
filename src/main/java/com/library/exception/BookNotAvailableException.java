package com.library.exception;

public class BookNotAvailableException extends RuntimeException {

    public BookNotAvailableException(String isbn) {
        super("No available copies for book with ISBN: " + isbn);
    }
}
