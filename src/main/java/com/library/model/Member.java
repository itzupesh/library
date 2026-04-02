package com.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Member {

    private final String memberId;
    private String name;
    private String email;
    private final LocalDate registrationDate;
    private final List<BorrowRecord> borrowHistory;

    public Member(String memberId, String name, String email) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.registrationDate = LocalDate.now();
        this.borrowHistory = new ArrayList<>();
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public List<BorrowRecord> getBorrowHistory() {
        return Collections.unmodifiableList(borrowHistory);
    }

    public void addBorrowRecord(BorrowRecord record) {
        borrowHistory.add(record);
    }

    public long getActiveBorrowCount() {
        return borrowHistory.stream()
                .filter(r -> r.getReturnDate() == null)
                .count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(memberId, member.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return String.format("Member{id='%s', name='%s', email='%s', activeBorrows=%d}",
                memberId, name, email, getActiveBorrowCount());
    }
}
