package universitiymanagementsystem.model;

public class ScholarshipApplication {
    private String id;
    private String studentName;
    private String department;
    private double gpa;
    private String applicationDate;
    private String status;

    public ScholarshipApplication(String id, String studentName, String department, double gpa, String applicationDate, String status) {
        this.id = id;
        this.studentName = studentName;
        this.department = department;
        this.gpa = gpa;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }
    public String getApplicationDate() { return applicationDate; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
