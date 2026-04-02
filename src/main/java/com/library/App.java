package com.library;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import com.library.service.LibraryService;

public class App {

    public static void main(String[] args) {
        LibraryService library = new LibraryService();

        System.out.println("=== Library Management System ===");
        System.out.println();

        // Add some books
        Book book1 = library.addBook("978-0-13-468599-1", "The Pragmatic Programmer", "David Thomas", 3);
        Book book2 = library.addBook("978-0-201-63361-0", "Design Patterns", "Gang of Four", 2);
        Book book3 = library.addBook("978-0-596-00712-6", "Head First Design Patterns", "Eric Freeman", 1);

        System.out.println("Books added:");
        library.getAllBooks().forEach(b -> System.out.println("  " + b));
        System.out.println();

        // Register members
        Member alice = library.registerMember("M001", "Alice Johnson", "alice@example.com");
        Member bob = library.registerMember("M002", "Bob Smith", "bob@example.com");

        System.out.println("Members registered:");
        library.getAllMembers().forEach(m -> System.out.println("  " + m));
        System.out.println();

        // Borrow books
        BorrowRecord record1 = library.borrowBook("M001", "978-0-13-468599-1");
        BorrowRecord record2 = library.borrowBook("M002", "978-0-201-63361-0");
        BorrowRecord record3 = library.borrowBook("M001", "978-0-596-00712-6");

        System.out.println("Borrow operations completed:");
        System.out.println("  " + record1);
        System.out.println("  " + record2);
        System.out.println("  " + record3);
        System.out.println();

        // Show available books
        System.out.println("Available books:");
        library.getAvailableBooks().forEach(b -> System.out.println("  " + b));
        System.out.println();

        // Return a book
        BorrowRecord returned = library.returnBook("M001", "978-0-13-468599-1");
        System.out.println("Book returned: " + returned);
        System.out.println();

        // Search
        System.out.println("Search results for 'Design':");
        library.searchBooksByTitle("Design").forEach(b -> System.out.println("  " + b));
        System.out.println();

        // Statistics
        System.out.println("=== Library Statistics ===");
        System.out.println("  Total books: " + library.getBookCount());
        System.out.println("  Total members: " + library.getMemberCount());
        System.out.println("  Active borrows: " + library.getActiveBorrowCount());
    }
}
