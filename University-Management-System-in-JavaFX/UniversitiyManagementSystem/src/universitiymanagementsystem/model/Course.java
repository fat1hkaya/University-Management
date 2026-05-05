package universitiymanagementsystem.model;

public class Course {
    private final String code;
    private final String title;
    private final int credit;
    private final String department;
    private final int quota;
    private final String classroom;
    private final String schedule;

    public Course(String code, String title, int credit,
                  String department, int quota, String classroom, String schedule) {
        this.code = code;
        this.title = title;
        this.credit = credit;
        this.department = department;
        this.quota = quota;
        this.classroom = classroom;
        this.schedule = schedule;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCredit() {
        return credit;
    }

    public String getDepartment() {
        return department;
    }

    public int getQuota() {
        return quota;
    }

    public String getClassroom() {
        return classroom;
    }

    public String getSchedule() {
        return schedule;
    }
}
