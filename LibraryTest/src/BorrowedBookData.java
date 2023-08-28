
import java.time.LocalDate;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Mii si heng
 */
 class BorrowedBookData {

    private String title;
    private String studentID;
    private LocalDate borrowDate;
    private LocalDate ExpiryDate;

    public BorrowedBookData(String title, String studentID, LocalDate borrowDate, LocalDate ExpiryDate) {
        this.title = title;
        this.studentID = studentID;
        this.borrowDate = borrowDate;
        this.ExpiryDate = ExpiryDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(LocalDate ExpiryDate) {
        this.ExpiryDate = ExpiryDate;
    }
}

