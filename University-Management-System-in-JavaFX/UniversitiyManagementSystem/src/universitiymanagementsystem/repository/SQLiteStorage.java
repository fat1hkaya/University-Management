package universitiymanagementsystem.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import universitiymanagementsystem.model.Announcement;
import universitiymanagementsystem.model.Course;
import universitiymanagementsystem.model.Message;
import universitiymanagementsystem.model.ScholarshipApplication;
import universitiymanagementsystem.model.Student;
import universitiymanagementsystem.model.Teacher;

public class SQLiteStorage {

    private final String jdbcUrl;

    public SQLiteStorage() {
        Path dbPath = Paths.get(System.getProperty("user.home"), "universite_otomasyonu.db");
        this.jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        createTablesIfNotExist();
        createDefaultUsersIfNotExist();
    }

    public void createTablesIfNotExist() {
        String[] ddl = {
            "PRAGMA foreign_keys = ON",
            "CREATE TABLE IF NOT EXISTS students ("
                + "student_id TEXT PRIMARY KEY,"
                + "full_name TEXT NOT NULL,"
                + "gpa REAL NOT NULL DEFAULT 0,"
                + "semester TEXT NOT NULL DEFAULT '',"
                + "department TEXT NOT NULL DEFAULT '',"
                + "registration_date TEXT NOT NULL DEFAULT '',"
                + "ects INTEGER NOT NULL DEFAULT 0,"
                + "status TEXT NOT NULL DEFAULT 'Aktif')",
            "CREATE TABLE IF NOT EXISTS teachers ("
                + "teacher_id TEXT PRIMARY KEY,"
                + "full_name TEXT NOT NULL,"
                + "department TEXT NOT NULL DEFAULT '',"
                + "title TEXT NOT NULL DEFAULT '',"
                + "email TEXT NOT NULL DEFAULT '',"
                + "hire_date TEXT NOT NULL DEFAULT '')",
            "CREATE TABLE IF NOT EXISTS courses ("
                + "code TEXT PRIMARY KEY,"
                + "title TEXT NOT NULL,"
                + "credit INTEGER NOT NULL DEFAULT 0,"
                + "department TEXT NOT NULL DEFAULT '',"
                + "quota INTEGER NOT NULL DEFAULT 0,"
                + "classroom TEXT NOT NULL DEFAULT '',"
                + "schedule TEXT NOT NULL DEFAULT '')",
            "CREATE TABLE IF NOT EXISTS enrollments ("
                + "student_id TEXT NOT NULL,"
                + "course_code TEXT NOT NULL,"
                + "PRIMARY KEY (student_id, course_code),"
                + "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (course_code) REFERENCES courses(code) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS prerequisites ("
                + "prerequisite_code TEXT NOT NULL,"
                + "target_course_code TEXT NOT NULL,"
                + "PRIMARY KEY (prerequisite_code, target_course_code),"
                + "FOREIGN KEY (prerequisite_code) REFERENCES courses(code) ON DELETE CASCADE,"
                + "FOREIGN KEY (target_course_code) REFERENCES courses(code) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS announcements ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT NOT NULL,"
                + "content TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "author TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS messages ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "sender TEXT NOT NULL,"
                + "recipient TEXT NOT NULL,"
                + "subject TEXT NOT NULL,"
                + "content TEXT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "is_read INTEGER NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS scholarship_applications ("
                + "id TEXT PRIMARY KEY,"
                + "student_name TEXT NOT NULL,"
                + "department TEXT NOT NULL DEFAULT '',"
                + "gpa REAL NOT NULL DEFAULT 0,"
                + "application_date TEXT NOT NULL,"
                + "status TEXT NOT NULL DEFAULT 'Beklemede')",
            "CREATE TABLE IF NOT EXISTS activity_log ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "message TEXT NOT NULL,"
                + "order_index INTEGER NOT NULL)",
            "CREATE TABLE IF NOT EXISTS app_users ("
                + "username TEXT PRIMARY KEY,"
                + "password_hash TEXT NOT NULL,"
                + "role_name TEXT NOT NULL)"
        };

        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement s = c.createStatement()) {
            for (String sql : ddl) {
                s.execute(sql);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Veritabani semasi olusturulamadi: " + ex.getMessage(), ex);
        }
    }

    public boolean hasAnyData() {
        String sql = "SELECT (SELECT COUNT(*) FROM students) + (SELECT COUNT(*) FROM teachers) + (SELECT COUNT(*) FROM courses) AS total_count";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total_count") > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Veritabani kontrolu basarisiz: " + ex.getMessage(), ex);
        }
    }

    public Snapshot loadSnapshot() {
        Snapshot snapshot = new Snapshot();
        try (Connection c = DriverManager.getConnection(jdbcUrl)) {
            snapshot.students = readAll(c, "SELECT student_id, full_name, gpa, semester, department, registration_date, ects, status FROM students", this::mapStudent);
            snapshot.teachers = readAll(c, "SELECT teacher_id, full_name, department, title, email, hire_date FROM teachers", this::mapTeacher);
            snapshot.courses = readAll(c, "SELECT code, title, credit, department, quota, classroom, schedule FROM courses", this::mapCourse);
            snapshot.enrollments = readAll(c, "SELECT student_id, course_code FROM enrollments", this::mapStringPair);
            snapshot.prerequisites = readAll(c, "SELECT prerequisite_code, target_course_code FROM prerequisites", this::mapStringPair);
            snapshot.announcements = readAll(c, "SELECT title, content, date, author FROM announcements ORDER BY id DESC", this::mapAnnouncement);
            snapshot.messages = readAll(c, "SELECT sender, recipient, subject, content, date, is_read FROM messages ORDER BY id DESC", this::mapMessage);
            snapshot.scholarshipApplications = readAll(c, "SELECT id, student_name, department, gpa, application_date, status FROM scholarship_applications", this::mapScholarship);
            snapshot.activities = readAll(c, "SELECT message FROM activity_log ORDER BY order_index ASC", this::mapActivity);
            return snapshot;
        } catch (SQLException ex) {
            throw new RuntimeException("Veriler yuklenemedi: " + ex.getMessage(), ex);
        }
    }

    public void saveSnapshot(Snapshot snapshot) {
        try (Connection c = DriverManager.getConnection(jdbcUrl)) {
            c.setAutoCommit(false);
            try {
                truncateAll(c);
                writeStudents(c, snapshot.students);
                writeTeachers(c, snapshot.teachers);
                writeCourses(c, snapshot.courses);
                writeEnrollments(c, snapshot.enrollments);
                writePrerequisites(c, snapshot.prerequisites);
                writeAnnouncements(c, snapshot.announcements);
                writeMessages(c, snapshot.messages);
                writeScholarshipApplications(c, snapshot.scholarshipApplications);
                writeActivities(c, snapshot.activities);
                c.commit();
            } catch (SQLException e) {
                safeRollback(c);
                throw new RuntimeException("Veritabani kaydedilemedi: " + e.getMessage(), e);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Veritabani baglanti hatasi: " + ex.getMessage(), ex);
        }
    }

    public void clearAllData() {
        try (Connection c = DriverManager.getConnection(jdbcUrl)) {
            c.setAutoCommit(false);
            try {
                truncateAll(c);
                c.commit();
            } catch (SQLException e) {
                safeRollback(c);
                throw new RuntimeException("Veritabani temizlenemedi: " + e.getMessage(), e);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Veritabani baglanti hatasi: " + ex.getMessage(), ex);
        }
    }

    public void saveActivities(String[] activities) {
        try (Connection c = DriverManager.getConnection(jdbcUrl)) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement("DELETE FROM activity_log")) {
                del.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO activity_log(message, order_index) VALUES(?,?)")) {
                for (int i = 0; i < activities.length; i++) {
                    ps.setString(1, activities[i]);
                    ps.setInt(2, i);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Aktiviteler kaydedilemedi: " + ex.getMessage(), ex);
        }
    }

    public LoginRecord findUser(String username) {
        String sql = "SELECT username, password_hash, role_name FROM app_users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new LoginRecord(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role_name")
                );
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Kullanici dogrulama verisi okunamadi: " + ex.getMessage(), ex);
        }
    }

    public void upsertStudent(Student s) {
        execute("INSERT INTO students(student_id, full_name, gpa, semester, department, registration_date, ects, status) "
              + "VALUES(?,?,?,?,?,?,?,?) ON CONFLICT(student_id) DO UPDATE SET "
              + "full_name=excluded.full_name, gpa=excluded.gpa, semester=excluded.semester, "
              + "department=excluded.department, registration_date=excluded.registration_date, "
              + "ects=excluded.ects, status=excluded.status",
            ps -> {
                ps.setString(1, s.getId());
                ps.setString(2, s.getFullName());
                ps.setDouble(3, s.getGpa());
                ps.setString(4, s.getSemester());
                ps.setString(5, s.getDepartment());
                ps.setString(6, s.getRegistrationDate());
                ps.setInt(7, s.getEcts());
                ps.setString(8, s.getStatus());
            }, "Ogrenci kaydedilemedi");
    }

    public void deleteStudent(String studentId) {
        execute("DELETE FROM students WHERE student_id = ?", ps -> ps.setString(1, studentId), "Ogrenci silinemedi");
    }

    public void upsertTeacher(Teacher t) {
        execute("INSERT INTO teachers(teacher_id, full_name, department, title, email, hire_date) "
              + "VALUES(?,?,?,?,?,?) ON CONFLICT(teacher_id) DO UPDATE SET "
              + "full_name=excluded.full_name, department=excluded.department, title=excluded.title, "
              + "email=excluded.email, hire_date=excluded.hire_date",
            ps -> {
                ps.setString(1, t.getId());
                ps.setString(2, t.getFullName());
                ps.setString(3, t.getDepartment());
                ps.setString(4, t.getTitle());
                ps.setString(5, t.getEmail());
                ps.setString(6, t.getHireDate());
            }, "Ogretim elemani kaydedilemedi");
    }

    public void deleteTeacher(String teacherId) {
        execute("DELETE FROM teachers WHERE teacher_id = ?", ps -> ps.setString(1, teacherId), "Ogretim elemani silinemedi");
    }

    public void upsertCourse(Course c) {
        execute("INSERT INTO courses(code, title, credit, department, quota, classroom, schedule) "
              + "VALUES(?,?,?,?,?,?,?) ON CONFLICT(code) DO UPDATE SET "
              + "title=excluded.title, credit=excluded.credit, department=excluded.department, "
              + "quota=excluded.quota, classroom=excluded.classroom, schedule=excluded.schedule",
            ps -> {
                ps.setString(1, c.getCode());
                ps.setString(2, c.getTitle());
                ps.setInt(3, c.getCredit());
                ps.setString(4, c.getDepartment());
                ps.setInt(5, c.getQuota());
                ps.setString(6, c.getClassroom());
                ps.setString(7, c.getSchedule());
            }, "Ders kaydedilemedi");
    }

    public void deleteCourse(String code) {
        execute("DELETE FROM courses WHERE code = ?", ps -> ps.setString(1, code), "Ders silinemedi");
    }

    public void addEnrollment(String studentId, String courseCode) {
        execute("INSERT OR IGNORE INTO enrollments(student_id, course_code) VALUES(?,?)",
            ps -> { ps.setString(1, studentId); ps.setString(2, courseCode); }, "Kayit eklenemedi");
    }

    public void addPrerequisite(String prereq, String target) {
        execute("INSERT OR IGNORE INTO prerequisites(prerequisite_code, target_course_code) VALUES(?,?)",
            ps -> { ps.setString(1, prereq); ps.setString(2, target); }, "Onkosul eklenemedi");
    }

    /**
     * Veritabanı tamamen boşsa (öğrenci, öğretmen, ders yok) örnek veriler ekler.
     * Uygulama ilk açıldığında ekranda hemen veri görünmesini sağlar.
     */
    public void insertInitialDataIfEmpty() {
        if (hasAnyData()) {
            return; // Zaten veri var, tekrar ekleme
        }
        try (Connection c = DriverManager.getConnection(jdbcUrl)) {
            c.setAutoCommit(false);
            try {
                // --- 3 Örnek Öğrenci ---
                String studentSql = "INSERT INTO students(student_id, full_name, gpa, semester, department, registration_date, ects, status) VALUES(?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(studentSql)) {
                    Object[][] students = {
                        {"2021001", "Ahmet Yılmaz",   3.75, "7", "Bilgisayar Mühendisliği", "2021-09-15", 210, "Aktif"},
                        {"2022045", "Zeynep Kaya",    3.20, "5", "Elektrik-Elektronik Müh.", "2022-09-12", 150, "Aktif"},
                        {"2023112", "Mehmet Demir",   2.85, "3", "Endüstri Mühendisliği",   "2023-09-10",  90, "Aktif"}
                    };
                    for (Object[] row : students) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setDouble(3, (Double) row[2]);
                        ps.setString(4, (String) row[3]);
                        ps.setString(5, (String) row[4]);
                        ps.setString(6, (String) row[5]);
                        ps.setInt(7, (Integer) row[6]);
                        ps.setString(8, (String) row[7]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // --- 2 Örnek Öğretim Elemanı ---
                String teacherSql = "INSERT INTO teachers(teacher_id, full_name, department, title, email, hire_date) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(teacherSql)) {
                    Object[][] teachers = {
                        {"T001", "Prof. Dr. Ali Çelik",   "Bilgisayar Mühendisliği", "Profesör",  "ali.celik@marmara.edu.tr",   "2010-09-01"},
                        {"T002", "Doç. Dr. Fatma Şahin",  "Elektrik-Elektronik Müh.", "Doçent",   "fatma.sahin@marmara.edu.tr", "2015-03-15"}
                    };
                    for (Object[] row : teachers) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setString(3, (String) row[2]);
                        ps.setString(4, (String) row[3]);
                        ps.setString(5, (String) row[4]);
                        ps.setString(6, (String) row[5]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // --- 3 Örnek Ders ---
                String courseSql = "INSERT INTO courses(code, title, credit, department, quota, classroom, schedule) VALUES(?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(courseSql)) {
                    Object[][] courses = {
                        {"BIL301", "Veri Yapıları ve Algoritmalar", 3, "Bilgisayar Mühendisliği", 60, "B-Blok 201", "Pazartesi 09:00"},
                        {"EEE201", "Devre Analizi",                 4, "Elektrik-Elektronik Müh.", 50, "A-Blok 105", "Salı 13:00"},
                        {"END101", "Mühendislik Matematiği",        3, "Endüstri Mühendisliği",   70, "C-Blok 301", "Çarşamba 10:00"}
                    };
                    for (Object[] row : courses) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setInt(3, (Integer) row[2]);
                        ps.setString(4, (String) row[3]);
                        ps.setInt(5, (Integer) row[4]);
                        ps.setString(6, (String) row[5]);
                        ps.setString(7, (String) row[6]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // --- 2 Örnek Duyuru ---
                String annSql = "INSERT INTO announcements(title, content, date, author) VALUES(?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(annSql)) {
                    Object[][] anns = {
                        {"2025-2026 Bahar Dönemi Kayıtları", "Bahar dönemi ders kayıtları 10 Şubat'ta başlayacaktır.", "2026-02-01", "Öğrenci İşleri"},
                        {"Burs Başvuruları Açıldı",          "Yüksek başarı bursu başvuruları için son tarih 30 Mayıs.", "2026-05-01", "Burs Birimi"}
                    };
                    for (Object[] row : anns) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setString(3, (String) row[2]);
                        ps.setString(4, (String) row[3]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // --- 2 Örnek Mesaj ---
                String msgSql = "INSERT INTO messages(sender, recipient, subject, content, date, is_read) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(msgSql)) {
                    Object[][] msgs = {
                        {"admin", "Ahmet Yılmaz", "Danışman Görüşmesi", "Lütfen bu hafta danışmanınızla görüşün.", "2026-05-02", 0},
                        {"Zeynep Kaya", "admin",  "Transkript Talebi",  "Resmi transkript belgesi talep ediyorum.", "2026-05-03", 0}
                    };
                    for (Object[] row : msgs) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setString(3, (String) row[2]);
                        ps.setString(4, (String) row[3]);
                        ps.setString(5, (String) row[4]);
                        ps.setInt(6, (Integer) row[5]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
            } catch (SQLException e) {
                safeRollback(c);
                throw new RuntimeException("Örnek veri eklenemedi: " + e.getMessage(), e);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Örnek veri bağlantı hatası: " + ex.getMessage(), ex);
        }
    }

    private void createDefaultUsersIfNotExist() {
        createUserIfAbsent("admin", "admin123", "Yonetici");
        createUserIfAbsent("ogrenci", "ogrenci123", "Ogrenci Isleri");
        createUserIfAbsent("akademik", "akademik123", "Akademik Personel");
    }

    private void createUserIfAbsent(String username, String plainPassword, String roleName) {
        String checkSql = "SELECT COUNT(*) AS cnt FROM app_users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement check = c.prepareStatement(checkSql)) {
            check.setString(1, username);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    return;
                }
            }
            try (PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO app_users(username, password_hash, role_name) VALUES(?,?,?)")) {
                insert.setString(1, username);
                insert.setString(2, BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)));
                insert.setString(3, roleName);
                insert.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Varsayilan kullanici olusturulamadi: " + ex.getMessage(), ex);
        }
    }

    private void truncateAll(Connection c) throws SQLException {
        try (Statement stmt = c.createStatement()) {
            stmt.executeUpdate("DELETE FROM enrollments");
            stmt.executeUpdate("DELETE FROM prerequisites");
            stmt.executeUpdate("DELETE FROM announcements");
            stmt.executeUpdate("DELETE FROM messages");
            stmt.executeUpdate("DELETE FROM scholarship_applications");
            stmt.executeUpdate("DELETE FROM activity_log");
            stmt.executeUpdate("DELETE FROM students");
            stmt.executeUpdate("DELETE FROM teachers");
            stmt.executeUpdate("DELETE FROM courses");
        }
    }

    private void safeRollback(Connection c) {
        try { c.rollback(); } catch (SQLException ignored) { }
    }

    private <T> List<T> readAll(Connection c, String sql, ResultSetMapper<T> mapper) throws SQLException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapper.map(rs));
            }
        }
        return list;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getString("student_id"),
            rs.getString("full_name"),
            rs.getDouble("gpa"),
            rs.getString("semester"),
            rs.getString("department"),
            rs.getString("registration_date"),
            rs.getInt("ects"),
            rs.getString("status")
        );
    }

    private Teacher mapTeacher(ResultSet rs) throws SQLException {
        return new Teacher(
            rs.getString("teacher_id"),
            rs.getString("full_name"),
            rs.getString("department"),
            rs.getString("title"),
            rs.getString("email"),
            rs.getString("hire_date")
        );
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        return new Course(
            rs.getString("code"),
            rs.getString("title"),
            rs.getInt("credit"),
            rs.getString("department"),
            rs.getInt("quota"),
            rs.getString("classroom"),
            rs.getString("schedule")
        );
    }

    private String[] mapStringPair(ResultSet rs) throws SQLException {
        return new String[]{rs.getString(1), rs.getString(2)};
    }

    private Announcement mapAnnouncement(ResultSet rs) throws SQLException {
        return new Announcement(
            rs.getString("title"),
            rs.getString("content"),
            rs.getString("date"),
            rs.getString("author")
        );
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        return new Message(
            rs.getString("sender"),
            rs.getString("recipient"),
            rs.getString("subject"),
            rs.getString("content"),
            rs.getString("date"),
            rs.getInt("is_read") == 1
        );
    }

    private ScholarshipApplication mapScholarship(ResultSet rs) throws SQLException {
        return new ScholarshipApplication(
            rs.getString("id"),
            rs.getString("student_name"),
            rs.getString("department"),
            rs.getDouble("gpa"),
            rs.getString("application_date"),
            rs.getString("status")
        );
    }

    private String mapActivity(ResultSet rs) throws SQLException {
        return rs.getString("message");
    }

    private void writeStudents(Connection c, List<Student> items) throws SQLException {
        batchInsert(c, "INSERT INTO students(student_id, full_name, gpa, semester, department, registration_date, ects, status) VALUES(?,?,?,?,?,?,?,?)",
            items, (ps, s) -> {
                ps.setString(1, s.getId());
                ps.setString(2, s.getFullName());
                ps.setDouble(3, s.getGpa());
                ps.setString(4, s.getSemester());
                ps.setString(5, s.getDepartment());
                ps.setString(6, s.getRegistrationDate());
                ps.setInt(7, s.getEcts());
                ps.setString(8, s.getStatus());
            });
    }

    private void writeTeachers(Connection c, List<Teacher> items) throws SQLException {
        batchInsert(c, "INSERT INTO teachers(teacher_id, full_name, department, title, email, hire_date) VALUES(?,?,?,?,?,?)",
            items, (ps, t) -> {
                ps.setString(1, t.getId());
                ps.setString(2, t.getFullName());
                ps.setString(3, t.getDepartment());
                ps.setString(4, t.getTitle());
                ps.setString(5, t.getEmail());
                ps.setString(6, t.getHireDate());
            });
    }

    private void writeCourses(Connection c, List<Course> items) throws SQLException {
        batchInsert(c, "INSERT INTO courses(code, title, credit, department, quota, classroom, schedule) VALUES(?,?,?,?,?,?,?)",
            items, (ps, co) -> {
                ps.setString(1, co.getCode());
                ps.setString(2, co.getTitle());
                ps.setInt(3, co.getCredit());
                ps.setString(4, co.getDepartment());
                ps.setInt(5, co.getQuota());
                ps.setString(6, co.getClassroom());
                ps.setString(7, co.getSchedule());
            });
    }

    private void writeEnrollments(Connection c, List<String[]> items) throws SQLException {
        batchInsert(c, "INSERT INTO enrollments(student_id, course_code) VALUES(?,?)",
            items, (ps, pair) -> { ps.setString(1, pair[0]); ps.setString(2, pair[1]); });
    }

    private void writePrerequisites(Connection c, List<String[]> items) throws SQLException {
        batchInsert(c, "INSERT INTO prerequisites(prerequisite_code, target_course_code) VALUES(?,?)",
            items, (ps, pair) -> { ps.setString(1, pair[0]); ps.setString(2, pair[1]); });
    }

    private void writeAnnouncements(Connection c, List<Announcement> items) throws SQLException {
        batchInsert(c, "INSERT INTO announcements(title, content, date, author) VALUES(?,?,?,?)",
            items, (ps, a) -> {
                ps.setString(1, a.getTitle());
                ps.setString(2, a.getContent());
                ps.setString(3, a.getDate());
                ps.setString(4, a.getAuthor());
            });
    }

    private void writeMessages(Connection c, List<Message> items) throws SQLException {
        batchInsert(c, "INSERT INTO messages(sender, recipient, subject, content, date, is_read) VALUES(?,?,?,?,?,?)",
            items, (ps, m) -> {
                ps.setString(1, m.getSender());
                ps.setString(2, m.getRecipient());
                ps.setString(3, m.getSubject());
                ps.setString(4, m.getContent());
                ps.setString(5, m.getDate());
                ps.setInt(6, m.isRead() ? 1 : 0);
            });
    }

    private void writeScholarshipApplications(Connection c, List<ScholarshipApplication> items) throws SQLException {
        batchInsert(c, "INSERT INTO scholarship_applications(id, student_name, department, gpa, application_date, status) VALUES(?,?,?,?,?,?)",
            items, (ps, app) -> {
                ps.setString(1, app.getId());
                ps.setString(2, app.getStudentName());
                ps.setString(3, app.getDepartment());
                ps.setDouble(4, app.getGpa());
                ps.setString(5, app.getApplicationDate());
                ps.setString(6, app.getStatus());
            });
    }

    private void writeActivities(Connection c, List<String> items) throws SQLException {
        if (items == null || items.isEmpty()) return;
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO activity_log(message, order_index) VALUES(?,?)")) {
            for (int i = 0; i < items.size(); i++) {
                ps.setString(1, items.get(i));
                ps.setInt(2, i);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private <T> void batchInsert(Connection c, String sql, List<T> items, BatchBinder<T> binder) throws SQLException {
        if (items == null || items.isEmpty()) return;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (T item : items) {
                binder.bind(ps, item);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void execute(String sql, PreparedStatementBinder binder, String errorMessage) {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(errorMessage + ": " + ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    private interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    private interface BatchBinder<T> {
        void bind(PreparedStatement ps, T item) throws SQLException;
    }

    @FunctionalInterface
    private interface PreparedStatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    public static final class Snapshot {
        public List<Student> students = new ArrayList<>();
        public List<Teacher> teachers = new ArrayList<>();
        public List<Course> courses = new ArrayList<>();
        public List<String[]> enrollments = new ArrayList<>();
        public List<String[]> prerequisites = new ArrayList<>();
        public List<Announcement> announcements = new ArrayList<>();
        public List<Message> messages = new ArrayList<>();
        public List<ScholarshipApplication> scholarshipApplications = new ArrayList<>();
        public List<String> activities = new ArrayList<>();
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

        public String getUsername() { return username; }
        public String getPasswordHash() { return passwordHash; }
        public String getRoleName() { return roleName; }
    }
}
