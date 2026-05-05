package universitiymanagementsystem.viewmodel;

public class ExamRow {
    public final String course;
    public final String date;
    public final int priority;
    public final String type;

    public ExamRow(String course, String date, int priority, String type) {
        this.course = course;
        this.date = date;
        this.priority = priority;
        this.type = type;
    }
}
