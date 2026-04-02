package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
    }

    @Nested
    @DisplayName("Book Management")
    class BookManagement {

        @Test
        @DisplayName("should add a new book")
        void addNewBook() {
            Book book = service.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 3);

            assertNotNull(book);
            assertEquals("978-0-13-468599-1", book.getIsbn());
            assertEquals("The Pragmatic Programmer", book.getTitle());
            assertEquals("David Thomas", book.getAuthor());
            assertEquals(3, book.getTotalCopies());
            assertEquals(3, book.getAvailableCopies());
        }

        @Test
        @DisplayName("should add copies to existing book")
        void addCopiesToExistingBook() {
            service.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 2);
            Book updated = service.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 3);

            assertEquals(5, updated.getTotalCopies());
            assertEquals(5, updated.getAvailableCopies());
        }

        @Test
        @DisplayName("should get book by ISBN")
        void getBookByIsbn() {
            service.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 1);

            Book found = service.getBook("978-0-13-468599-1");
            assertEquals("The Pragmatic Programmer", found.getTitle());
        }

        @Test
        @DisplayName("should throw when getting non-existent book")
        void getBookNotFound() {
            assertThrows(BookNotFoundException.class, () -> service.getBook("INVALID"));
        }

        @Test
        @DisplayName("should remove book with no active borrows")
        void removeBook() {
            service.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 1);

            assertTrue(service.removeBook("978-0-13-468599-1"));
            assertEquals(0, service.getBookCount());
        }

        @Test
        @DisplayName("should not remove book with active borrows")
        void removeBookWithActiveBorrows() {
            service.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 1);
            service.registerMember("M001", "Alice", "alice@test.com");
            service.borrowBook("M001", "978-0-13-468599-1");

            assertFalse(service.removeBook("978-0-13-468599-1"));
            assertEquals(1, service.getBookCount());
        }

        @Test
        @DisplayName("should throw when removing non-existent book")
        void removeNonExistentBook() {
            assertThrows(BookNotFoundException.class, () -> service.removeBook("INVALID"));
        }

        @Test
        @DisplayName("should list all books")
        void getAllBooks() {
            service.addBook("ISBN-1", "Book One", "Author A", 1);
            service.addBook("ISBN-2", "Book Two", "Author B", 1);

            List<Book> books = service.getAllBooks();
            assertEquals(2, books.size());
        }

        @Test
        @DisplayName("should search books by title")
        void searchByTitle() {
            service.addBook("ISBN-1", "Java Programming", "Author A", 1);
            service.addBook("ISBN-2", "Python Programming", "Author B", 1);
            service.addBook("ISBN-3", "Design Patterns", "Author C", 1);

            List<Book> results = service.searchBooksByTitle("Programming");
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("should search books by author")
        void searchByAuthor() {
            service.addBook("ISBN-1", "Book One", "Martin Fowler", 1);
            service.addBook("ISBN-2", "Book Two", "Robert Martin", 1);
            service.addBook("ISBN-3", "Book Three", "Kent Beck", 1);

            List<Book> results = service.searchBooksByAuthor("Martin");
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("should return only available books")
        void getAvailableBooks() {
            service.addBook("ISBN-1", "Book One", "Author A", 1);
            service.addBook("ISBN-2", "Book Two", "Author B", 1);
            service.registerMember("M001", "Alice", "alice@test.com");
            service.borrowBook("M001", "ISBN-1");

            List<Book> available = service.getAvailableBooks();
            assertEquals(1, available.size());
            assertEquals("ISBN-2", available.get(0).getIsbn());
        }
    }

    @Nested
    @DisplayName("Member Management")
    class MemberManagement {

        @Test
        @DisplayName("should register a new member")
        void registerMember() {
            Member member = service.registerMember("M001", "Alice Johnson", "alice@test.com");

            assertNotNull(member);
            assertEquals("M001", member.getMemberId());
            assertEquals("Alice Johnson", member.getName());
            assertEquals("alice@test.com", member.getEmail());
        }

        @Test
        @DisplayName("should throw when registering duplicate member")
        void registerDuplicateMember() {
            service.registerMember("M001", "Alice", "alice@test.com");

            assertThrows(IllegalArgumentException.class,
                    () -> service.registerMember("M001", "Bob", "bob@test.com"));
        }

        @Test
        @DisplayName("should get member by ID")
        void getMemberById() {
            service.registerMember("M001", "Alice", "alice@test.com");

            Member found = service.getMember("M001");
            assertEquals("Alice", found.getName());
        }

        @Test
        @DisplayName("should throw when getting non-existent member")
        void getMemberNotFound() {
            assertThrows(MemberNotFoundException.class, () -> service.getMember("INVALID"));
        }

        @Test
        @DisplayName("should list all members")
        void getAllMembers() {
            service.registerMember("M001", "Alice", "alice@test.com");
            service.registerMember("M002", "Bob", "bob@test.com");

            List<Member> members = service.getAllMembers();
            assertEquals(2, members.size());
        }
    }

    @Nested
    @DisplayName("Borrow and Return")
    class BorrowReturn {

        @BeforeEach
        void setUpBooksAndMembers() {
            service.addBook("ISBN-1", "Book One", "Author A", 2);
            service.addBook("ISBN-2", "Book Two", "Author B", 1);
            service.registerMember("M001", "Alice", "alice@test.com");
            service.registerMember("M002", "Bob", "bob@test.com");
        }

        @Test
        @DisplayName("should borrow a book successfully")
        void borrowBook() {
            BorrowRecord record = service.borrowBook("M001", "ISBN-1");

            assertNotNull(record);
            assertEquals("M001", record.getMemberId());
            assertEquals("ISBN-1", record.getIsbn());
            assertNotNull(record.getBorrowDate());
            assertNotNull(record.getDueDate());
            assertNull(record.getReturnDate());
            assertFalse(record.isReturned());

            Book book = service.getBook("ISBN-1");
            assertEquals(1, book.getAvailableCopies());
        }

        @Test
        @DisplayName("should throw when borrowing unavailable book")
        void borrowUnavailableBook() {
            service.borrowBook("M001", "ISBN-2"); // Only 1 copy

            assertThrows(BookNotAvailableException.class,
                    () -> service.borrowBook("M002", "ISBN-2"));
        }

        @Test
        @DisplayName("should throw when member already has same book")
        void borrowDuplicateBook() {
            service.borrowBook("M001", "ISBN-1");

            assertThrows(IllegalStateException.class,
                    () -> service.borrowBook("M001", "ISBN-1"));
        }

        @Test
        @DisplayName("should throw when borrow limit exceeded")
        void borrowLimitExceeded() {
            service.addBook("ISBN-3", "Book Three", "Author C", 1);
            service.addBook("ISBN-4", "Book Four", "Author D", 1);
            service.addBook("ISBN-5", "Book Five", "Author E", 1);
            service.addBook("ISBN-6", "Book Six", "Author F", 1);

            service.borrowBook("M001", "ISBN-1");
            service.borrowBook("M001", "ISBN-2");
            service.borrowBook("M001", "ISBN-3");
            service.borrowBook("M001", "ISBN-4");
            service.borrowBook("M001", "ISBN-5");

            assertThrows(BorrowLimitExceededException.class,
                    () -> service.borrowBook("M001", "ISBN-6"));
        }

        @Test
        @DisplayName("should throw when borrowing for non-existent member")
        void borrowNonExistentMember() {
            assertThrows(MemberNotFoundException.class,
                    () -> service.borrowBook("INVALID", "ISBN-1"));
        }

        @Test
        @DisplayName("should throw when borrowing non-existent book")
        void borrowNonExistentBook() {
            assertThrows(BookNotFoundException.class,
                    () -> service.borrowBook("M001", "INVALID"));
        }

        @Test
        @DisplayName("should return a book successfully")
        void returnBook() {
            service.borrowBook("M001", "ISBN-1");
            BorrowRecord record = service.returnBook("M001", "ISBN-1");

            assertNotNull(record.getReturnDate());
            assertTrue(record.isReturned());

            Book book = service.getBook("ISBN-1");
            assertEquals(2, book.getAvailableCopies());
        }

        @Test
        @DisplayName("should throw when returning book not borrowed")
        void returnNotBorrowed() {
            assertThrows(IllegalStateException.class,
                    () -> service.returnBook("M001", "ISBN-1"));
        }

        @Test
        @DisplayName("should get member borrow history")
        void getMemberBorrowHistory() {
            service.borrowBook("M001", "ISBN-1");
            service.borrowBook("M001", "ISBN-2");

            List<BorrowRecord> history = service.getMemberBorrowHistory("M001");
            assertEquals(2, history.size());
        }

        @Test
        @DisplayName("should track active borrow count")
        void activeBorrowCount() {
            service.borrowBook("M001", "ISBN-1");
            service.borrowBook("M002", "ISBN-2");

            assertEquals(2, service.getActiveBorrowCount());

            service.returnBook("M001", "ISBN-1");
            assertEquals(1, service.getActiveBorrowCount());
        }
    }

    @Nested
    @DisplayName("Statistics")
    class Statistics {

        @Test
        @DisplayName("should return correct book count")
        void bookCount() {
            assertEquals(0, service.getBookCount());
            service.addBook("ISBN-1", "Book One", "Author A", 1);
            assertEquals(1, service.getBookCount());
        }

        @Test
        @DisplayName("should return correct member count")
        void memberCount() {
            assertEquals(0, service.getMemberCount());
            service.registerMember("M001", "Alice", "alice@test.com");
            assertEquals(1, service.getMemberCount());
        }
    }
}
