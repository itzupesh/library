package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryService {

    private static final int MAX_BORROW_LIMIT = 5;

    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final List<BorrowRecord> borrowRecords = new ArrayList<>();

    // ---- Book operations ----

    public Book addBook(String isbn, String title, String author, int copies) {
        if (books.containsKey(isbn)) {
            Book existing = books.get(isbn);
            existing.setTotalCopies(existing.getTotalCopies() + copies);
            existing.setAvailableCopies(existing.getAvailableCopies() + copies);
            return existing;
        }
        Book book = new Book(isbn, title, author, copies);
        books.put(isbn, book);
        return book;
    }

    public Book getBook(String isbn) {
        Book book = books.get(isbn);
        if (book == null) {
            throw new BookNotFoundException(isbn);
        }
        return book;
    }

    public boolean removeBook(String isbn) {
        if (!books.containsKey(isbn)) {
            throw new BookNotFoundException(isbn);
        }
        boolean hasBorrows = borrowRecords.stream()
                .anyMatch(r -> r.getIsbn().equals(isbn) && !r.isReturned());
        if (hasBorrows) {
            return false;
        }
        books.remove(isbn);
        return true;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public List<Book> searchBooksByTitle(String keyword) {
        String lower = keyword.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooksByAuthor(String author) {
        String lower = author.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> getAvailableBooks() {
        return books.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    // ---- Member operations ----

    public Member registerMember(String memberId, String name, String email) {
        if (members.containsKey(memberId)) {
            throw new IllegalArgumentException("Member already exists with ID: " + memberId);
        }
        Member member = new Member(memberId, name, email);
        members.put(memberId, member);
        return member;
    }

    public Member getMember(String memberId) {
        Member member = members.get(memberId);
        if (member == null) {
            throw new MemberNotFoundException(memberId);
        }
        return member;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members.values());
    }

    // ---- Borrow / Return operations ----

    public BorrowRecord borrowBook(String memberId, String isbn) {
        Member member = getMember(memberId);
        Book book = getBook(isbn);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(isbn);
        }

        if (member.getActiveBorrowCount() >= MAX_BORROW_LIMIT) {
            throw new BorrowLimitExceededException(memberId, MAX_BORROW_LIMIT);
        }

        // Check if member already has this book
        boolean alreadyBorrowed = borrowRecords.stream()
                .anyMatch(r -> r.getMemberId().equals(memberId)
                        && r.getIsbn().equals(isbn)
                        && !r.isReturned());
        if (alreadyBorrowed) {
            throw new IllegalStateException("Member already has this book borrowed");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        BorrowRecord record = new BorrowRecord(memberId, isbn);
        borrowRecords.add(record);
        member.addBorrowRecord(record);

        return record;
    }

    public BorrowRecord returnBook(String memberId, String isbn) {
        getMember(memberId);
        Book book = getBook(isbn);

        BorrowRecord activeRecord = borrowRecords.stream()
                .filter(r -> r.getMemberId().equals(memberId)
                        && r.getIsbn().equals(isbn)
                        && !r.isReturned())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No active borrow record found for member " + memberId + " and ISBN " + isbn));

        activeRecord.setReturnDate(LocalDate.now());
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        return activeRecord;
    }

    public List<BorrowRecord> getMemberBorrowHistory(String memberId) {
        getMember(memberId);
        return borrowRecords.stream()
                .filter(r -> r.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getOverdueRecords() {
        return borrowRecords.stream()
                .filter(r -> !r.isReturned() && r.isOverdue())
                .collect(Collectors.toList());
    }

    public int getBookCount() {
        return books.size();
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getActiveBorrowCount() {
        return (int) borrowRecords.stream().filter(r -> !r.isReturned()).count();
    }
}
