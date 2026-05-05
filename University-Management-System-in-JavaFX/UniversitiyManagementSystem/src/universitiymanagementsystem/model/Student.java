package universitiymanagementsystem.model;

public class Student {
    private final String id;
    private final String fullName;
    private final double gpa;
    private final String semester;
    private final String department;
    private final String registrationDate;
    private final int ects;
    private final String status;

    public Student(String id, String fullName, double gpa, String semester,
                   String department, String registrationDate, int ects, String status) {
        this.id = id;
        this.fullName = fullName;
        this.gpa = gpa;
        this.semester = semester;
        this.department = department;
        this.registrationDate = registrationDate;
        this.ects = ects;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public double getGpa() {
        return gpa;
    }

    public String getSemester() {
        return semester;
    }

    public String getDepartment() {
        return department;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public int getEcts() {
        return ects;
    }

    public String getStatus() {
        return status;
    }
}
