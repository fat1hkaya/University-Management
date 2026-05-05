package universitiymanagementsystem.model;

public class GpaRecord {
    private final String semester;
    private final double gpa;

    public GpaRecord(String semester, double gpa) {
        this.semester = semester;
        this.gpa = gpa;
    }

    public String getSemester() {
        return semester;
    }

    public double getGpa() {
        return gpa;
    }
}