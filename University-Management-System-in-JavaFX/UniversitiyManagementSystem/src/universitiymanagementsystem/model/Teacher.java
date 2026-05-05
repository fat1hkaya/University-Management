package universitiymanagementsystem.model;

public class Teacher {
    private final String id;
    private final String fullName;
    private final String department;
    private final String title;
    private final String email;
    private final String hireDate;

    public Teacher(String id, String fullName, String department,
                   String title, String email, String hireDate) {
        this.id = id;
        this.fullName = fullName;
        this.department = department;
        this.title = title;
        this.email = email;
        this.hireDate = hireDate;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public String getTitle() {
        return title;
    }

    public String getEmail() {
        return email;
    }

    public String getHireDate() {
        return hireDate;
    }
}
