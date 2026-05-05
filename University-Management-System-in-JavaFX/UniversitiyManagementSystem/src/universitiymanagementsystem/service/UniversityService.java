package universitiymanagementsystem.service;

import org.mindrot.jbcrypt.BCrypt;
import universitiymanagementsystem.model.Announcement;
import universitiymanagementsystem.model.Course;
import universitiymanagementsystem.model.GpaRecord;
import universitiymanagementsystem.model.Message;
import universitiymanagementsystem.model.ScholarshipApplication;
import universitiymanagementsystem.model.Student;
import universitiymanagementsystem.model.Teacher;
import java.util.Random;
import universitiymanagementsystem.repository.SQLiteStorage;
import universitiymanagementsystem.repository.UniversityRepository;

public class UniversityService {

    private final UniversityRepository repository = new UniversityRepository();
    private final SQLiteStorage storage = new SQLiteStorage();
    private String currentUsername;
    private String currentRole;

    public void initialize() {
        repository.loadOrSeed();
    }

    public boolean login(String username, String password) {
        SQLiteStorage.LoginRecord user = storage.findUser(username);
        if (user == null) {
            return false;
        }
        boolean ok = BCrypt.checkpw(password, user.getPasswordHash());
        if (ok) {
            currentUsername = user.getUsername();
            currentRole = user.getRoleName();
        }
        return ok;
    }

    public String roleOf(String username) {
        return currentRole == null ? "Kullanici" : currentRole;
    }

    public Student[] students() {
        return repository.getStudents();
    }

    public Teacher[] teachers() {
        return repository.getTeachers();
    }

    public Course[] courses() {
        return repository.getCourses();
    }

    public int studentCount() {
        return repository.studentCount();
    }

    public int teacherCount() {
        return repository.teacherCount();
    }

    public int courseCount() {
        return repository.courseCount();
    }

    public String topStudentDisplay() {
        return repository.topStudentDisplay();
    }

    public String[] recentActivities() {
        return repository.getActivitySnapshot();
    }

    public boolean addStudent(Student student) {
        if (!ValidationUtil.validId(student.getId()) || !ValidationUtil.validName(student.getFullName())) {
            return false;
        }
        return repository.addStudent(student);
    }

    public boolean addTeacher(Teacher teacher) {
        if (!ValidationUtil.validId(teacher.getId())
                || !ValidationUtil.validName(teacher.getFullName())
                || !ValidationUtil.validDepartment(teacher.getDepartment())) {
            return false;
        }
        return repository.addTeacher(teacher);
    }

    public boolean addCourse(Course course) {
        if (!ValidationUtil.validCourseCode(course.getCode()) || course.getCredit() <= 0) {
            return false;
        }
        return repository.addCourse(course);
    }

    public boolean addPrerequisite(String prerequisite, String target) {
        if (!ValidationUtil.validCourseCode(prerequisite) || !ValidationUtil.validCourseCode(target)) {
            return false;
        }
        return repository.addPrerequisite(prerequisite, target);
    }

    public boolean enrollStudent(String studentId, String courseCode) {
        if (!ValidationUtil.validId(studentId) || !ValidationUtil.validCourseCode(courseCode)) {
            return false;
        }
        return repository.enrollStudent(studentId, courseCode);
    }

    public boolean undo() {
        return repository.undoLastOperation();
    }

    public boolean redo() {
        return repository.redoLastOperation();
    }

    public String[] buildPlan(String studentId) {
        return repository.buildCoursePlanForStudent(studentId);
    }

    public Student[] studentsSortedByGpa() {
        return repository.getStudentsSortedByGpa();
    }

    public Student[] studentsSortedByName() {
        return repository.getStudentsSortedByName();
    }

    public String[] prerequisiteEdges() {
        return repository.getPrerequisiteEdges();
    }

    public boolean hasCycleInPrerequisites() {
        return repository.hasCycleInPrerequisites();
    }

    public void enqueueScholarship(ScholarshipApplication app) {
        repository.enqueueScholarship(app);
    }

    public ScholarshipApplication dequeueScholarship() {
        return repository.dequeueScholarship();
    }

    public ScholarshipApplication[] scholarshipQueue() {
        return repository.scholarshipQueueArray();
    }

    public void pushAnnouncement(Announcement ann) {
        repository.pushAnnouncement(ann);
    }

    public Announcement popAnnouncement() {
        return repository.popAnnouncement();
    }

    public Announcement[] announcementStack() {
        return repository.announcementStackArray();
    }

    public boolean deleteStudent(String id) {
        return repository.deleteStudent(id);
    }

    public boolean deleteTeacher(String id) {
        return repository.deleteTeacher(id);
    }

    public boolean deleteCourse(String code) {
        return repository.deleteCourse(code);
    }

    public void sendMessage(Message msg) {
        repository.sendMessage(msg);
    }

    public Message[] messages() {
        return repository.messageArray();
    }

    public GpaRecord[] getGpaHistory() {
        Student[] all = repository.getStudents();
        String[] semesters = {"2023-Güz", "2024-Bahar", "2024-Güz", "2025-Bahar", "2025-Güz", "2026-Bahar"};
        GpaRecord[] records = new GpaRecord[semesters.length];
        
        if (all.length == 0) {
            records[0] = new GpaRecord("2023-Güz", 2.45);
            records[1] = new GpaRecord("2024-Bahar", 2.62);
            records[2] = new GpaRecord("2024-Güz", 2.78);
            records[3] = new GpaRecord("2025-Bahar", 2.95);
            records[4] = new GpaRecord("2025-Güz", 3.12);
            records[5] = new GpaRecord("2026-Bahar", 3.28);
            return records;
        }
        
        Random rand = new Random(42);
        for (int i = 0; i < semesters.length; i++) {
            double sum = 0;
            for (Student s : all) {
                double base = s.getGpa();
                double variation = (i - semesters.length / 2.0) * 0.08 + (rand.nextDouble() - 0.5) * 0.3;
                double semGpa = Math.max(1.0, Math.min(4.0, base + variation));
                sum += semGpa;
            }
            records[i] = new GpaRecord(semesters[i], sum / all.length);
        }
        return records;
    }

    public GpaRecord[] getStudentGpaHistory(String studentId) {
        Student student = null;
        for (Student s : repository.getStudents()) {
            if (s.getId().equals(studentId)) {
                student = s;
                break;
            }
        }
        if (student == null) {
            return new GpaRecord[0];
        }
        String[] semesters = {"2023-Güz", "2024-Bahar", "2024-Güz", "2025-Bahar", "2025-Güz", "2026-Bahar"};
        GpaRecord[] records = new GpaRecord[semesters.length];
        Random rand = new Random(studentId.hashCode());
        double base = student.getGpa();
        for (int i = 0; i < semesters.length; i++) {
            double variation = (i - semesters.length / 2.0) * 0.1 + (rand.nextDouble() - 0.5) * 0.4;
            double semGpa = Math.max(1.0, Math.min(4.0, base + variation));
            records[i] = new GpaRecord(semesters[i], Math.round(semGpa * 100.0) / 100.0);
        }
        return records;
    }

    public void shutdown() {
        // No manual save needed as operations are granular and sync now
    }
}
