
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Mii si heng
 */
class ReturnBook extends BorrowBook {

    private LocalDate returnDate;
    private double penalty;

    public ReturnBook(String title, String author, String studentID, LocalDate returnDate) {
        super(title, author, studentID, returnDate.minusDays(7)); // Expiry date is return date - 7 days
        this.returnDate = returnDate;
        this.penalty = 0;
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

    public void calculatePenalty() {
        if (returnDate.isAfter(calculateExpiryDate())) {
            long daysLate = ChronoUnit.DAYS.between(calculateExpiryDate(), returnDate);
            penalty = daysLate * 5; // Penalty of 5 ringgit per day late
        }
    }
}
