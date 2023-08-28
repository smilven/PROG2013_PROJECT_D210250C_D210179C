
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


interface LibraryItem {
    String getTitle();
    void setTitle(String title);
    String getAuthor();
     void setAuthor(String Author);
}












public abstract class LibraryGUI {

    private static Library library;
    private static JComboBox<Book> bookComboBox;
    private static JTextArea outputArea;

    public static void main(String[] args) {
        library = new Library();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new LibraryFrame("Library Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    static class LibraryFrame extends JFrame {

        private JTextField titleField;
        private JTextField authorField;
        private JTextField studentIDField;

        public LibraryFrame(String title) {
            super(title);

            titleField = new JTextField(20);
            authorField = new JTextField(20);
            studentIDField = new JTextField(10);

            JButton addButton = new JButton("Add Book");
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String title = titleField.getText();
                    String author = authorField.getText();
                    Book book = new Book(title, author);

                    addBookToDatabase(title, author); // Store in the database

                    library.addBook(book);
                    updateBookComboBox();
                    clearFields();
                }
            });

            bookComboBox = new JComboBox<>();
            bookComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Book selectedBook = (Book) e.getItem();
                        if (selectedBook != null) {
                            titleField.setText(selectedBook.getTitle());
                            authorField.setText(selectedBook.getAuthor());
                        }
                    }
                }
            });
            updateBookComboBox();

            JButton borrowButton = new JButton("Borrow Book");
            // Inside the Borrow Book button ActionListener
            borrowButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Book selectedBook = (Book) bookComboBox.getSelectedItem();
                    String studentID = studentIDField.getText();

                    if (selectedBook != null && !studentID.isEmpty()) {
                        BorrowBook borrowedBook = new BorrowBook(selectedBook.getTitle(), selectedBook.getAuthor(), studentID, LocalDate.now());
                        addBorrowingToDatabase(selectedBook.getTitle(), studentID, borrowedBook.getBorrowDate(), borrowedBook.calculateExpiryDate());
                        library.removeBook(selectedBook); // Remove the borrowed book from the library
                        updateBookComboBox(); // Update the book combo box
                        String output = "Borrowed book: " + borrowedBook.getTitle() + "\n"
                                + "Student ID: " + borrowedBook.getStudentID() + "\n"
                                + "Borrow Date: " + borrowedBook.getBorrowDate() + "\n"
                                + "Expiry Date: " + borrowedBook.calculateExpiryDate();
                        outputArea.setText(output);
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter a valid student ID.");
                    }
                }
            });

            JButton editButton = new JButton("Edit Book");
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Book selectedBook = (Book) bookComboBox.getSelectedItem();
                    if (selectedBook != null) {
                        String newTitle = titleField.getText();
                        String newAuthor = authorField.getText();
                        editBookInDatabase(selectedBook, newTitle, newAuthor);
                        updateBookComboBox();
                        clearFields();
                    }
                }
            });

            JButton deleteButton = new JButton("Delete Book");
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Book selectedBook = (Book) bookComboBox.getSelectedItem();
                    if (selectedBook != null) {
                        deleteBookFromDatabase(selectedBook);
                        library.removeBook(selectedBook);
                        updateBookComboBox();
                        clearFields();
                    }
                }
            });

            JButton viewBorrowedButton = new JButton("View Borrowed Books");
            viewBorrowedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    displayBorrowedBooksTable();
                }
            });

            JButton returnButton = new JButton("Return Book");
            returnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String studentID = studentIDField.getText();
                    Book selectedBook = (Book) bookComboBox.getSelectedItem();
                    LocalDate returnDate = LocalDate.now();

                    if (selectedBook != null && !studentID.isEmpty()) {
                        // Check if the book is borrowed
                        if (isBookBorrowed(selectedBook.getTitle(), studentID)) {
                            BorrowedBookData borrowedBook = getBorrowedBook(selectedBook.getTitle(), studentID);
                            LocalDate expiryDate = borrowedBook.getExpiryDate();

                            // Calculate penalty if return date is late
                            double penalty = 0;
                            if (returnDate.isAfter(expiryDate)) {
                                long daysLate = ChronoUnit.DAYS.between(expiryDate, returnDate);
                                penalty = daysLate * 5; // Penalty of 5 ringgit per day late
                            }

                            // Delete borrowed book data and update GUI
                            deleteBorrowedBook(selectedBook.getTitle(), studentID);
                            updateBookComboBox();

                            // Store return data in the database
                            addReturnToDatabase(selectedBook.getTitle(), selectedBook.getAuthor(), studentID, returnDate, penalty);

                            if (penalty > 0) {
                                JOptionPane.showMessageDialog(null, "Book returned with a penalty of " + penalty + " ringgit.");
                            } else {
                                JOptionPane.showMessageDialog(null, "Book returned successfully.");
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "This book was not borrowed by the provided student ID.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter a valid student ID.");
                    }
                }
            });
            JButton viewReturnedButton = new JButton("View Returned Books");
            viewReturnedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    displayReturnedBooksTable();
                }
            });
            outputArea = new JTextArea(10, 30);
            outputArea.setEditable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(7, 2));
            panel.add(new JLabel("Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Author:"));
            panel.add(authorField);
            panel.add(new JLabel("Student ID:"));
            panel.add(studentIDField);
            panel.add(addButton);
            panel.add(bookComboBox);
            panel.add(editButton);
            panel.add(deleteButton);
            panel.add(borrowButton);
            panel.add(viewBorrowedButton);
            panel.add(returnButton);
            panel.add(viewReturnedButton);

            JScrollPane scrollPane = new JScrollPane(outputArea);

            Container contentPane = getContentPane();
            contentPane.add(panel, BorderLayout.NORTH);
            contentPane.add(scrollPane, BorderLayout.CENTER);
        }

        private void addBookToDatabase(String title, String author) {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String insertQuery = "INSERT INTO books (title, author) VALUES (?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void updateBookComboBox() {
            bookComboBox.removeAllItems();
            List<Book> booksFromDatabase = fetchBooksFromDatabase();
            for (Book book : booksFromDatabase) {
                bookComboBox.addItem(book);
            }
        }

        private List<Book> fetchBooksFromDatabase() {
            List<Book> books = new ArrayList<>();
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String selectQuery = "SELECT title, author FROM books";
                PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                var resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    Book book = new Book(title, author);
                    books.add(book);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            return books;
        }

        private void editBookInDatabase(Book book, String newTitle, String newAuthor) {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";
            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String updateQuery = "UPDATE books SET title = ?, author = ? WHERE title = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, newTitle);
                preparedStatement.setString(2, newAuthor);
                preparedStatement.setString(3, book.getTitle());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void deleteBookFromDatabase(Book book) {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String deleteQuery = "DELETE FROM books WHERE title = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, book.getTitle());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void addBorrowingToDatabase(String bookTitle, String studentID, LocalDate borrowDate, LocalDate ExpiryDate) {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String insertQuery = "INSERT INTO borrowings (book_title, student_id, borrow_date, expire_date) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.setString(1, bookTitle);
                preparedStatement.setString(2, studentID);
                preparedStatement.setDate(3, java.sql.Date.valueOf(borrowDate));
                preparedStatement.setDate(4, java.sql.Date.valueOf(ExpiryDate));
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private List<BorrowedBookData> fetchBorrowedBooksFromDatabase() {
            List<BorrowedBookData> borrowedBooks = new ArrayList<>();
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String selectQuery = "SELECT book_title,student_id, borrow_date,expire_date FROM borrowings";
                PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                var resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String title = resultSet.getString("book_title");
                    String studentID = resultSet.getString("student_id");
                    LocalDate borrowDate = resultSet.getDate("borrow_date").toLocalDate();
                    LocalDate ExpiryDate = resultSet.getDate("expire_date").toLocalDate();
                    borrowedBooks.add(new BorrowedBookData(title, studentID, borrowDate, ExpiryDate));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            return borrowedBooks;
        }

        private void displayBorrowedBooksTable() {
            List<BorrowedBookData> borrowedBooks = fetchBorrowedBooksFromDatabase();
            Object[][] data = new Object[borrowedBooks.size()][4];

            for (int i = 0; i < borrowedBooks.size(); i++) {
                BorrowedBookData borrowedBook = borrowedBooks.get(i);
                data[i][0] = borrowedBook.getTitle();
                data[i][1] = borrowedBook.getStudentID();
                data[i][2] = borrowedBook.getBorrowDate();
                data[i][3] = borrowedBook.getExpiryDate();
            }

            Object[] columnNames = {"Title", "Student ID", "Borrow Date", "Expire Date"};
            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JFrame tableFrame = new JFrame("Borrowed Books");
            tableFrame.getContentPane().add(scrollPane);
            tableFrame.pack();
            tableFrame.setVisible(true);
        }

        private boolean isBookBorrowed(String title, String studentID) {
            List<BorrowedBookData> borrowedBooks = fetchBorrowedBooksFromDatabase();
            for (BorrowedBookData borrowedBook : borrowedBooks) {
                if (borrowedBook.getTitle().equals(title) && borrowedBook.getStudentID().equals(studentID)) {
                    return true;
                }
            }
            return false;
        }

        private BorrowedBookData getBorrowedBook(String title, String studentID) {
            List<BorrowedBookData> borrowedBooks = fetchBorrowedBooksFromDatabase();
            for (BorrowedBookData borrowedBook : borrowedBooks) {
                if (borrowedBook.getTitle().equals(title) && borrowedBook.getStudentID().equals(studentID)) {
                    return borrowedBook;
                }
            }
            return null;
        }

        private void deleteBorrowedBook(String title, String studentID) {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String deleteQuery = "DELETE FROM borrowings WHERE book_title = ? AND student_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, studentID);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void addReturnToDatabase(String bookTitle, String bookAuthor, String studentID, LocalDate returnDate, double penalty) {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String insertQuery = "INSERT INTO returns (book_title, book_author, student_id, return_date, penalty) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                preparedStatement.setString(1, bookTitle);
                preparedStatement.setString(2, bookAuthor);
                preparedStatement.setString(3, studentID);
                preparedStatement.setDate(4, java.sql.Date.valueOf(returnDate));
                preparedStatement.setDouble(5, penalty);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private List<ReturnedBookData> fetchReturnedBooksFromDatabase() {
            List<ReturnedBookData> returnedBooks = new ArrayList<>();
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "";

            try ( Connection connection = DriverManager.getConnection(url, user, password)) {
                String selectQuery = "SELECT book_title, book_author, student_id, return_date, penalty FROM returns";
                PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                var resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String bookTitle = resultSet.getString("book_title");
                    String bookAuthor = resultSet.getString("book_author");
                    String studentID = resultSet.getString("student_id");
                    LocalDate returnDate = resultSet.getDate("return_date").toLocalDate();
                    double penalty = resultSet.getDouble("penalty");
                    returnedBooks.add(new ReturnedBookData(bookTitle, bookAuthor, studentID, returnDate, penalty));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            return returnedBooks;
        }

        private void displayReturnedBooksTable() {
            List<ReturnedBookData> returnedBooks = fetchReturnedBooksFromDatabase();
            Object[][] data = new Object[returnedBooks.size()][5];

            for (int i = 0; i < returnedBooks.size(); i++) {
                ReturnedBookData returnedBook = returnedBooks.get(i);
                data[i][0] = returnedBook.getBookTitle();
                data[i][1] = returnedBook.getBookAuthor();
                data[i][2] = returnedBook.getStudentID();
                data[i][3] = returnedBook.getReturnDate();
                data[i][4] = returnedBook.getPenalty();
            }

            Object[] columnNames = {"Book Title", "Book Author", "Student ID", "Return Date", "Penalty"};
            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JFrame tableFrame = new JFrame("Returned Books");
            tableFrame.getContentPane().add(scrollPane);
            tableFrame.pack();
            tableFrame.setVisible(true);
        }

        private void clearFields() {
            titleField.setText("");
            authorField.setText("");
            studentIDField.setText("");
        }
    }
}
