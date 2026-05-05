package universitiymanagementsystem.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import universitiymanagementsystem.datastructures.DoublyLinkedList;
import org.mindrot.jbcrypt.BCrypt;
import universitiymanagementsystem.model.Course;
import universitiymanagementsystem.model.Student;
import universitiymanagementsystem.model.Teacher;
import java.util.ArrayList;
import java.util.List;

public class SQLiteStorage {

    private final String jdbcUrl;
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SQLiteStorage() {
        Path dbPath = Paths.get(System.getProperty("user.home"), "universite_otomasyonu.db");
        this.jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        initializeSchema();
        ensureDefaultUsers();
    }

    public boolean hasAnyData() {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM students) + "
                + "(SELECT COUNT(*) FROM teachers) + "
                + "(SELECT COUNT(*) FROM courses) AS total_count";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() && resultSet.getInt("total_count") > 0;
        } catch (Exception ex) {
            throw new RuntimeException("Veri tabanı kontrolü başarısız.", ex);
        }
    }

    public Snapshot loadSnapshot() {
        Snapshot snapshot = new Snapshot();
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            readStudents(connection, snapshot.students);
            readTeachers(connection, snapshot.teachers);
            readCourses(connection, snapshot.courses);
            readEnrollments(connection, snapshot.enrollments);
            readPrerequisites(connection, snapshot.prerequisites);
            readActivities(connection, snapshot.activities);
            return snapshot;
        } catch (Exception ex) {
            throw new RuntimeException("Veriler yüklenemedi.", ex);
        }
    }

    public void upsertStudent(Student s) {
        String sql = "INSERT INTO students(student_id, full_name, gpa, semester, department, registration_date, ects, status) " +
                     "VALUES(?,?,?,?,?,?,?,?) ON CONFLICT(student_id) DO UPDATE SET " +
                     "full_name=excluded.full_name, gpa=excluded.gpa, semester=excluded.semester, " +
                     "department=excluded.department, registration_date=excluded.registration_date, " +
                     "ects=excluded.ects, status=excluded.status";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getId());
            ps.setString(2, s.getFullName());
            ps.setDouble(3, s.getGpa());
            ps.setString(4, s.getSemester());
            ps.setString(5, s.getDepartment());
            ps.setString(6, s.getRegistrationDate());
            ps.setInt(7, s.getEcts());
            ps.setString(8, s.getStatus());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Öğrenci kaydedilemedi.", ex);
        }
    }

    public void deleteStudent(String studentId) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
            ps.setString(1, studentId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Öğrenci silinemedi.", ex);
        }
    }

    public void upsertTeacher(Teacher t) {
        String sql = "INSERT INTO teachers(teacher_id, full_name, department, title, email, hire_date) " +
                     "VALUES(?,?,?,?,?,?) ON CONFLICT(teacher_id) DO UPDATE SET " +
                     "full_name=excluded.full_name, department=excluded.department, " +
                     "title=excluded.title, email=excluded.email, hire_date=excluded.hire_date";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, t.getId());
            ps.setString(2, t.getFullName());
            ps.setString(3, t.getDepartment());
            ps.setString(4, t.getTitle());
            ps.setString(5, t.getEmail());
            ps.setString(6, t.getHireDate());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Öğretmen kaydedilemedi.", ex);
        }
    }

    public void deleteTeacher(String teacherId) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement("DELETE FROM teachers WHERE teacher_id = ?")) {
            ps.setString(1, teacherId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Öğretmen silinemedi.", ex);
        }
    }

    public void upsertCourse(Course c) {
        String sql = "INSERT INTO courses(code, title, credit, department, quota, classroom, schedule) " +
                     "VALUES(?,?,?,?,?,?,?) ON CONFLICT(code) DO UPDATE SET " +
                     "title=excluded.title, credit=excluded.credit, department=excluded.department, " +
                     "quota=excluded.quota, classroom=excluded.classroom, schedule=excluded.schedule";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredit());
            ps.setString(4, c.getDepartment());
            ps.setInt(5, c.getQuota());
            ps.setString(6, c.getClassroom());
            ps.setString(7, c.getSchedule());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Ders kaydedilemedi.", ex);
        }
    }

    public void deleteCourse(String code) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement("DELETE FROM courses WHERE code = ?")) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Ders silinemedi.", ex);
        }
    }

    public void addEnrollment(String studentId, String courseCode) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO enrollments(student_id, course_code) VALUES(?,?)")) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Kayıt eklenemedi.", ex);
        }
    }

    public void addPrerequisite(String prereq, String target) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO prerequisites(prerequisite_code, target_course_code) VALUES(?,?)")) {
            ps.setString(1, prereq);
            ps.setString(2, target);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Ön koşul eklenemedi.", ex);
        }
    }

    public void saveActivities(String[] activities) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            try (Statement st = connection.createStatement()) { st.executeUpdate("DELETE FROM activity_log"); }
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO activity_log(message, order_index) VALUES(?,?)")) {
                for (int i = 0; i < activities.length; i++) {
                    ps.setString(1, activities[i]);
                    ps.setInt(2, i);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Aktiviteler kaydedilemedi.", ex);
        }
    }

    private void initializeSchema() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS students ("
                    + "student_id TEXT PRIMARY KEY,"
                    + "full_name TEXT NOT NULL,"
                    + "gpa REAL NOT NULL,"
                    + "semester TEXT NOT NULL,"
                    + "department TEXT NOT NULL DEFAULT '',"
                    + "registration_date TEXT NOT NULL DEFAULT '',"
                    + "ects INTEGER NOT NULL DEFAULT 0,"
                    + "status TEXT NOT NULL DEFAULT 'Aktif')");
            statement.execute("CREATE TABLE IF NOT EXISTS teachers ("
                    + "teacher_id TEXT PRIMARY KEY,"
                    + "full_name TEXT NOT NULL,"
                    + "department TEXT NOT NULL,"
                    + "title TEXT NOT NULL DEFAULT '',"
                    + "email TEXT NOT NULL DEFAULT '',"
                    + "hire_date TEXT NOT NULL DEFAULT '')");
            statement.execute("CREATE TABLE IF NOT EXISTS courses ("
                    + "code TEXT PRIMARY KEY,"
                    + "title TEXT NOT NULL,"
                    + "credit INTEGER NOT NULL,"
                    + "department TEXT NOT NULL DEFAULT '',"
                    + "quota INTEGER NOT NULL DEFAULT 0,"
                    + "classroom TEXT NOT NULL DEFAULT '',"
                    + "schedule TEXT NOT NULL DEFAULT '')");
            statement.execute("CREATE TABLE IF NOT EXISTS enrollments ("
                    + "student_id TEXT NOT NULL,"
                    + "course_code TEXT NOT NULL,"
                    + "PRIMARY KEY(student_id, course_code))");
            statement.execute("CREATE TABLE IF NOT EXISTS prerequisites ("
                    + "prerequisite_code TEXT NOT NULL,"
                    + "target_course_code TEXT NOT NULL,"
                    + "PRIMARY KEY(prerequisite_code, target_course_code))");
            statement.execute("CREATE TABLE IF NOT EXISTS activity_log ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "message TEXT NOT NULL,"
                    + "order_index INTEGER NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS app_users ("
                    + "username TEXT PRIMARY KEY,"
                    + "password_hash TEXT NOT NULL,"
                    + "role_name TEXT NOT NULL)");
            migrateIfNeeded(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Veri tabanı şeması oluşturulamadı.", ex);
        }
    }

    private void migrateIfNeeded(Connection connection) throws Exception {
        String[] studentCols = {"department", "registration_date", "ects", "status"};
        for (String col : studentCols) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE students ADD COLUMN " + col + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {}
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE students ALTER COLUMN ects TYPE INTEGER");
        } catch (Exception ignored) {}
        String[] teacherCols = {"title", "email", "hire_date"};
        for (String col : teacherCols) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE teachers ADD COLUMN " + col + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {}
        }
        String[] courseCols = {"department", "quota", "classroom", "schedule"};
        for (String col : courseCols) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE courses ADD COLUMN " + col + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {}
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE courses ADD COLUMN quota INTEGER NOT NULL DEFAULT 0");
        } catch (Exception ignored) {}
    }

    private void ensureDefaultUsers() {
        createUserIfAbsent("admin", "admin123", "Yönetici");
        createUserIfAbsent("ogrenci", "ogrenci123", "Öğrenci İşleri");
        createUserIfAbsent("akademik", "akademik123", "Akademik Personel");
    }

    private void createUserIfAbsent(String username, String plainPassword, String roleName) {
        String checkSql = "SELECT COUNT(*) AS cnt FROM app_users WHERE username = ?";
        String insertSql = "INSERT INTO app_users(username, password_hash, role_name) VALUES(?,?,?)";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement check = connection.prepareStatement(checkSql)) {
            check.setString(1, username);
            ResultSet rs = check.executeQuery();
            boolean exists = rs.next() && rs.getInt("cnt") > 0;
            if (exists) {
                return;
            }
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                insert.setString(1, username);
                insert.setString(2, BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)));
                insert.setString(3, roleName);
                insert.executeUpdate();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Varsayılan kullanıcılar oluşturulamadı.", ex);
        }
    }

    public LoginRecord findUser(String username) {
        String sql = "SELECT username, password_hash, role_name FROM app_users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new LoginRecord(rs.getString("username"), rs.getString("password_hash"), rs.getString("role_name"));
        } catch (Exception ex) {
            throw new RuntimeException("Kullanıcı doğrulama verisi okunamadı.", ex);
        }
    }

    public void saveSnapshot(Snapshot snapshot) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            truncateAll(connection);
            writeStudents(connection, snapshot.students);
            writeTeachers(connection, snapshot.teachers);
            writeCourses(connection, snapshot.courses);
            writeEnrollments(connection, snapshot.enrollments);
            writePrerequisites(connection, snapshot.prerequisites);
            writeActivities(connection, snapshot.activities);
            connection.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Veri tabanı kaydedilemedi.", ex);
        }
    }

    public void clearAllData() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            truncateAll(connection);
        } catch (Exception ex) {
            throw new RuntimeException("Veri tabanı temizlenemedi.", ex);
        }
    }

    private void truncateAll(Connection connection) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM students");
            stmt.executeUpdate("DELETE FROM teachers");
            stmt.executeUpdate("DELETE FROM courses");
            stmt.executeUpdate("DELETE FROM enrollments");
            stmt.executeUpdate("DELETE FROM prerequisites");
            stmt.executeUpdate("DELETE FROM activity_log");
        }
    }

    private void writeStudents(Connection connection, List<Student> students) throws Exception {
        String sql = "INSERT INTO students(student_id, full_name, gpa, semester, department, registration_date, ects, status) VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Student s : students) {
                ps.setString(1, s.getId());
                ps.setString(2, s.getFullName());
                ps.setDouble(3, s.getGpa());
                ps.setString(4, s.getSemester());
                ps.setString(5, s.getDepartment());
                ps.setString(6, s.getRegistrationDate());
                ps.setInt(7, s.getEcts());
                ps.setString(8, s.getStatus());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void writeTeachers(Connection connection, List<Teacher> teachers) throws Exception {
        String sql = "INSERT INTO teachers(teacher_id, full_name, department, title, email, hire_date) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Teacher t : teachers) {
                ps.setString(1, t.getId());
                ps.setString(2, t.getFullName());
                ps.setString(3, t.getDepartment());
                ps.setString(4, t.getTitle());
                ps.setString(5, t.getEmail());
                ps.setString(6, t.getHireDate());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void writeCourses(Connection connection, List<Course> courses) throws Exception {
        String sql = "INSERT INTO courses(code, title, credit, department, quota, classroom, schedule) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Course c : courses) {
                ps.setString(1, c.getCode());
                ps.setString(2, c.getTitle());
                ps.setInt(3, c.getCredit());
                ps.setString(4, c.getDepartment());
                ps.setInt(5, c.getQuota());
                ps.setString(6, c.getClassroom());
                ps.setString(7, c.getSchedule());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void writeEnrollments(Connection connection, List<String[]> enrollments) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO enrollments(student_id, course_code) VALUES(?,?)")) {
            for (String[] pair : enrollments) {
                ps.setString(1, pair[0]);
                ps.setString(2, pair[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void writePrerequisites(Connection connection, List<String[]> prerequisites) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO prerequisites(prerequisite_code, target_course_code) VALUES(?,?)")) {
            for (String[] pair : prerequisites) {
                ps.setString(1, pair[0]);
                ps.setString(2, pair[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void readStudents(Connection connection, List<Student> target) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT student_id, full_name, gpa, semester, department, registration_date, ects, status FROM students");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                target.add(new Student(
                    rs.getString("student_id"),
                    rs.getString("full_name"),
                    rs.getDouble("gpa"),
                    rs.getString("semester"),
                    rs.getString("department"),
                    rs.getString("registration_date"),
                    rs.getInt("ects"),
                    rs.getString("status")
                ));
            }
        }
    }

    private void readTeachers(Connection connection, List<Teacher> target) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT teacher_id, full_name, department, title, email, hire_date FROM teachers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                target.add(new Teacher(
                    rs.getString("teacher_id"),
                    rs.getString("full_name"),
                    rs.getString("department"),
                    rs.getString("title"),
                    rs.getString("email"),
                    rs.getString("hire_date")
                ));
            }
        }
    }

    private void readCourses(Connection connection, List<Course> target) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT code, title, credit, department, quota, classroom, schedule FROM courses");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                target.add(new Course(
                    rs.getString("code"),
                    rs.getString("title"),
                    rs.getInt("credit"),
                    rs.getString("department"),
                    rs.getInt("quota"),
                    rs.getString("classroom"),
                    rs.getString("schedule")
                ));
            }
        }
    }

    private void readEnrollments(Connection connection, List<String[]> target) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT student_id, course_code FROM enrollments");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                target.add(new String[]{rs.getString("student_id"), rs.getString("course_code")});
            }
        }
    }

    private void readPrerequisites(Connection connection, List<String[]> target) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT prerequisite_code, target_course_code FROM prerequisites");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                target.add(new String[]{rs.getString("prerequisite_code"), rs.getString("target_course_code")});
            }
        }
    }

    private void readActivities(Connection connection, List<String> target) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("SELECT message FROM activity_log ORDER BY order_index ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                target.add(rs.getString("message"));
            }
        }
    }

    private void writeActivities(Connection connection, List<String> activities) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO activity_log(message, order_index) VALUES(?,?)")) {
            for (int i = 0; i < activities.size(); i++) {
                ps.setString(1, activities.get(i));
                ps.setInt(2, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static final class Snapshot {
        public final List<Student> students = new ArrayList<>();
        public final List<Teacher> teachers = new ArrayList<>();
        public final List<Course> courses = new ArrayList<>();
        public final List<String[]> enrollments = new ArrayList<>();
        public final List<String[]> prerequisites = new ArrayList<>();
        public final List<String> activities = new ArrayList<>();
    }

    public static final class LoginRecord {
        private final String username;
        private final String passwordHash;
        private final String roleName;

        public LoginRecord(String username, String passwordHash, String roleName) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.roleName = roleName;
        }

        public String getUsername() {
            return username;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public String getRoleName() {
            return roleName;
        }
    }
}
