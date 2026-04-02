package com.library.model;

import java.time.LocalDate;

public class BorrowRecord {

    private final String memberId;
    private final String isbn;
    private final LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;

    public BorrowRecord(String memberId, String isbn) {
        this.memberId = memberId;
        this.isbn = isbn;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(14); // 2-week lending period
        this.returnDate = null;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getIsbn() {
        return isbn;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isOverdue() {
        if (returnDate != null) {
            return returnDate.isAfter(dueDate);
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    @Override
    public String toString() {
        return String.format("BorrowRecord{member='%s', isbn='%s', borrowed=%s, due=%s, returned=%s}",
                memberId, isbn, borrowDate, dueDate, returnDate);
    }
}
