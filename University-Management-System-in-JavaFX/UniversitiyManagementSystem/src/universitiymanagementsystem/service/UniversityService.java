package universitiymanagementsystem.service;

import org.mindrot.jbcrypt.BCrypt;
import universitiymanagementsystem.model.Announcement;
import universitiymanagementsystem.model.Course;
import universitiymanagementsystem.model.GpaRecord;
import universitiymanagementsystem.model.Message;
import universitiymanagementsystem.model.ScholarshipApplication;
import universitiymanagementsystem.model.Student;
import universitiymanagementsystem.model.Teacher;
import universitiymanagementsystem.repository.SQLiteStorage;
import universitiymanagementsystem.repository.UniversityRepository;

public class UniversityService {

    private final UniversityRepository repository = new UniversityRepository();
    private String currentUsername;
    private String currentRole;

    public void initialize() {
        repository.loadOrSeed();
    }

    public boolean login(String username, String password) {
        SQLiteStorage.LoginRecord user = repository.findUser(username);
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

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public String roleOf(String username) {
        SQLiteStorage.LoginRecord user = repository.findUser(username);
        return user == null ? "Kullanici" : user.getRoleName();
    }

    // ---------- Öğrenci Yönetimi (BST O(log n)) ----------
    public Student findStudentById(String id) {
        return repository.findStudentById(id);
    }

    public Student[] students() {
        return repository.getStudents();
    }

    public int studentCount() {
        return repository.studentCount();
    }

    public boolean addStudent(Student student) {
        if (!ValidationUtil.validId(student.getId()) || !ValidationUtil.validName(student.getFullName())) {
            return false;
        }
        return repository.addStudent(student);
    }

    public boolean deleteStudent(String id) {
        return repository.deleteStudent(id);
    }

    public Student[] studentsSortedByGpa() {
        return repository.getStudentsSortedByGpa();
    }

    public Student[] studentsSortedByName() {
        return repository.getStudentsSortedByName();
    }

    public Student[] getTopStudents(int count) {
        return repository.getTopStudents(count);
    }

    public String topStudentDisplay() {
        return repository.topStudentDisplay();
    }

    // ---------- Öğretim Elemanı Yönetimi ----------
    public Teacher[] teachers() {
        return repository.getTeachers();
    }

    public int teacherCount() {
        return repository.teacherCount();
    }

    public boolean addTeacher(Teacher teacher) {
        if (!ValidationUtil.validId(teacher.getId())
                || !ValidationUtil.validName(teacher.getFullName())
                || !ValidationUtil.validDepartment(teacher.getDepartment())) {
            return false;
        }
        return repository.addTeacher(teacher);
    }

    public boolean deleteTeacher(String id) {
        return repository.deleteTeacher(id);
    }

    // ---------- Ders ve Önkoşul Yönetimi (DirectedGraph) ----------
    public Course[] courses() {
        return repository.getCourses();
    }

    public int courseCount() {
        return repository.courseCount();
    }

    public boolean addCourse(Course course) {
        if (!ValidationUtil.validCourseCode(course.getCode()) || course.getCredit() <= 0) {
            return false;
        }
        return repository.addCourse(course);
    }

    public boolean deleteCourse(String code) {
        return repository.deleteCourse(code);
    }

    public boolean addPrerequisite(String prerequisite, String target) {
        if (!ValidationUtil.validCourseCode(prerequisite) || !ValidationUtil.validCourseCode(target)) {
            return false;
        }
        return repository.addPrerequisite(prerequisite, target);
    }

    public boolean canStudentTakeCourse(String studentId, String courseCode) {
        return repository.canEnroll(studentId, courseCode);
    }

    public boolean enrollStudent(String studentId, String courseCode) {
        if (!ValidationUtil.validId(studentId) || !ValidationUtil.validCourseCode(courseCode)) {
            return false;
        }
        return repository.enrollStudent(studentId, courseCode);
    }

    public String[] buildPlan(String studentId) {
        return repository.buildCoursePlanForStudent(studentId);
    }

    public String[] prerequisiteEdges() {
        return repository.getPrerequisiteEdges();
    }

    public boolean hasCycleInPrerequisites() {
        return repository.hasCycleInPrerequisites();
    }

    // ---------- Başarı ve Not Sıralaması (MaxHeap + Deterministik) ----------
    public GpaRecord[] getGpaHistory() {
        Student[] all = repository.getStudents();
        double baseAvg = 2.85;
        if (all.length > 0) {
            double sum = 0;
            for (Student s : all) {
                sum += s.getGpa();
            }
            baseAvg = sum / all.length;
        }
        String[] semesters = {"2023-Güz", "2024-Bahar", "2024-Güz", "2025-Bahar", "2025-Güz", "2026-Bahar"};
        GpaRecord[] records = new GpaRecord[semesters.length];
        for (int i = 0; i < semesters.length; i++) {
            double variation = (i - semesters.length / 2.0) * 0.08;
            double gpa = Math.max(1.0, Math.min(4.0, baseAvg + variation));
            records[i] = new GpaRecord(semesters[i], Math.round(gpa * 100.0) / 100.0);
        }
        return records;
    }

    public GpaRecord[] getStudentGpaHistory(String studentId) {
        Student student = findStudentById(studentId);
        if (student == null) {
            return new GpaRecord[0];
        }
        String[] semesters = {"2023-Güz", "2024-Bahar", "2024-Güz", "2025-Bahar", "2025-Güz", "2026-Bahar"};
        GpaRecord[] records = new GpaRecord[semesters.length];
        double base = student.getGpa();
        for (int i = 0; i < semesters.length; i++) {
            double variation = (i - semesters.length / 2.0) * 0.1;
            double semGpa = Math.max(1.0, Math.min(4.0, base + variation));
            records[i] = new GpaRecord(semesters[i], Math.round(semGpa * 100.0) / 100.0);
        }
        return records;
    }

    // ---------- Duyuru ve Mesajlaşma (CustomQueue / CustomStack) ----------
    public void sendMessage(Message msg) {
        repository.sendMessage(msg);
    }

    public Message[] messages() {
        return repository.messageArray();
    }

    public Message[] unreadMessagesFor(String recipient) {
        return repository.getUnreadMessages(recipient);
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

    public Announcement[] recentAnnouncements(int limit) {
        return repository.getRecentAnnouncements(limit);
    }

    // ---------- Burs Başvuruları (CustomQueue) ----------
    public void enqueueScholarship(ScholarshipApplication app) {
        repository.enqueueScholarship(app);
    }

    public ScholarshipApplication dequeueScholarship() {
        return repository.dequeueScholarship();
    }

    public ScholarshipApplication[] scholarshipQueue() {
        return repository.scholarshipQueueArray();
    }

    // ---------- İşlem Geçmişi / Geri Al (CustomStack / CustomQueue) ----------
    public boolean undo() {
        return repository.undoLastOperation();
    }

    public boolean redo() {
        return repository.redoLastOperation();
    }

    public String[] recentActivities() {
        return repository.getActivitySnapshot();
    }

    // ---------- Diğer ----------
    public void shutdown() {
        // Bellek yapıları zaten granular olarak senkronize
    }
}
