package universitiymanagementsystem.viewmodel;

public class GradeRow {
    public final String studentId;
    public final String studentName;
    public final String department;
    public final double gpa;
    public final String letterGrade;
    public final String status;

    public GradeRow(String studentId, String studentName, String department, double gpa, String letterGrade, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.gpa = gpa;
        this.letterGrade = letterGrade;
        this.status = status;
    }
}
