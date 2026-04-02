package com.library.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String memberId) {
        super("Member not found with ID: " + memberId);
    }
}
