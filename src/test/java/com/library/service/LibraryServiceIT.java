package com.library.service;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that exercise full borrow/return workflows.
 */
@DisplayName("Library Service Integration Tests")
class LibraryServiceIT {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
    }

    @Test
    @DisplayName("Full lifecycle: add books, register members, borrow, return, search")
    void fullLifecycle() {
        // Setup library catalog
        service.addBook("ISBN-001", "Clean Code", "Robert C. Martin", 3);
        service.addBook("ISBN-002", "Refactoring", "Martin Fowler", 2);
        service.addBook("ISBN-003", "Domain-Driven Design", "Eric Evans", 1);
        service.addBook("ISBN-004", "Clean Architecture", "Robert C. Martin", 2);

        assertEquals(4, service.getBookCount());

        // Register members
        service.registerMember("M100", "Alice Johnson", "alice@library.com");
        service.registerMember("M101", "Bob Williams", "bob@library.com");
        service.registerMember("M102", "Carol Davis", "carol@library.com");

        assertEquals(3, service.getMemberCount());

        // Borrowing phase
        service.borrowBook("M100", "ISBN-001");
        service.borrowBook("M100", "ISBN-002");
        service.borrowBook("M101", "ISBN-001");
        service.borrowBook("M102", "ISBN-003");

        assertEquals(4, service.getActiveBorrowCount());

        // Verify availability
        Book cleanCode = service.getBook("ISBN-001");
        assertEquals(1, cleanCode.getAvailableCopies()); // 3 total, 2 borrowed

        Book ddd = service.getBook("ISBN-003");
        assertEquals(0, ddd.getAvailableCopies()); // 1 total, 1 borrowed
        assertFalse(ddd.isAvailable());

        // Return phase
        service.returnBook("M100", "ISBN-001");
        assertEquals(3, service.getActiveBorrowCount());
        assertEquals(2, cleanCode.getAvailableCopies());

        // Search functionality
        List<Book> martinBooks = service.searchBooksByAuthor("Robert C. Martin");
        assertEquals(2, martinBooks.size()); // Clean Code + Clean Architecture

        List<Book> cleanBooks = service.searchBooksByTitle("Clean");
        assertEquals(2, cleanBooks.size()); // Clean Code + Clean Architecture

        // After returning, member can borrow again
        service.borrowBook("M100", "ISBN-004");
        assertEquals(4, service.getActiveBorrowCount());

        // Borrow history
        List<BorrowRecord> aliceHistory = service.getMemberBorrowHistory("M100");
        assertEquals(3, aliceHistory.size()); // ISBN-001, ISBN-002, ISBN-004
    }

    @Test
    @DisplayName("Multiple members borrowing and returning the same book")
    void concurrentBorrowing() {
        service.addBook("ISBN-001", "Popular Book", "Famous Author", 2);
        service.registerMember("M001", "Alice", "alice@test.com");
        service.registerMember("M002", "Bob", "bob@test.com");
        service.registerMember("M003", "Carol", "carol@test.com");

        // Two members borrow (2 copies available)
        service.borrowBook("M001", "ISBN-001");
        service.borrowBook("M002", "ISBN-001");

        // Third member can't borrow
        Book book = service.getBook("ISBN-001");
        assertFalse(book.isAvailable());

        // First member returns
        service.returnBook("M001", "ISBN-001");
        assertTrue(book.isAvailable());

        // Now third member can borrow
        service.borrowBook("M003", "ISBN-001");
        assertFalse(book.isAvailable());
    }

    @Test
    @DisplayName("Adding copies to an existing book increases availability")
    void addCopies() {
        service.addBook("ISBN-001", "Book", "Author", 1);
        service.registerMember("M001", "Alice", "alice@test.com");

        service.borrowBook("M001", "ISBN-001");
        assertFalse(service.getBook("ISBN-001").isAvailable());

        // Add more copies
        service.addBook("ISBN-001", "Book", "Author", 2);
        Book book = service.getBook("ISBN-001");
        assertEquals(3, book.getTotalCopies());
        assertEquals(2, book.getAvailableCopies());
        assertTrue(book.isAvailable());
    }

    @Test
    @DisplayName("Removing a book after all copies returned")
    void removeAfterReturns() {
        service.addBook("ISBN-001", "Temporary Book", "Author", 1);
        service.registerMember("M001", "Alice", "alice@test.com");

        service.borrowBook("M001", "ISBN-001");
        assertFalse(service.removeBook("ISBN-001")); // Can't remove - active borrow

        service.returnBook("M001", "ISBN-001");
        assertTrue(service.removeBook("ISBN-001")); // Now can remove
        assertEquals(0, service.getBookCount());
    }

    @Test
    @DisplayName("Available books filter works correctly through borrow/return cycles")
    void availableBooksFilter() {
        service.addBook("ISBN-001", "Book A", "Author", 1);
        service.addBook("ISBN-002", "Book B", "Author", 1);
        service.addBook("ISBN-003", "Book C", "Author", 1);
        service.registerMember("M001", "Alice", "alice@test.com");

        assertEquals(3, service.getAvailableBooks().size());

        service.borrowBook("M001", "ISBN-001");
        assertEquals(2, service.getAvailableBooks().size());

        service.borrowBook("M001", "ISBN-002");
        assertEquals(1, service.getAvailableBooks().size());

        service.returnBook("M001", "ISBN-001");
        assertEquals(2, service.getAvailableBooks().size());
    }
}
