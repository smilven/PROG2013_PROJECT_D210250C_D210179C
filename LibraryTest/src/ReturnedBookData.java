
import java.time.LocalDate;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Mii si heng
 */
class ReturnedBookData {

    private String bookTitle;
    private String bookAuthor;
    private String studentID;
    private LocalDate returnDate;
    private double penalty;

    public ReturnedBookData(String bookTitle, String bookAuthor, String studentID, LocalDate returnDate, double penalty) {
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.studentID = studentID;
        this.returnDate = returnDate;
        this.penalty = penalty;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }
}