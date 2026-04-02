package com.library.exception;

public class BorrowLimitExceededException extends RuntimeException {

    public BorrowLimitExceededException(String memberId, int limit) {
        super("Member " + memberId + " has reached the borrow limit of " + limit + " books");
    }
}
