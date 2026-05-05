package universitiymanagementsystem.repository;

import universitiymanagementsystem.datastructures.*;
import universitiymanagementsystem.model.Announcement;
import universitiymanagementsystem.model.Course;
import universitiymanagementsystem.model.Message;
import universitiymanagementsystem.model.ScholarshipApplication;
import universitiymanagementsystem.model.Student;
import universitiymanagementsystem.model.Teacher;

/**
 * Modernized Repository using custom data structures instead of java.util collections
 * for core business logic storage.
 */
public class UniversityRepository {

    private final BST<String, Student> studentsById = new BST<>();
    private final BST<String, Teacher> teachersById = new BST<>();
    private final BST<String, Course> coursesByCode = new BST<>();
    
    // Using BST where keys are Student IDs and values are Lists of Course Codes
    private final BST<String, DoublyLinkedList<String>> studentCourseIndex = new BST<>();

    private final MaxHeap<Student> topStudents = new MaxHeap<>(100, (left, right) -> Double.compare(left.getGpa(), right.getGpa()));
    private final DoublyLinkedList<String> activityLog = new DoublyLinkedList<>();
    private final DirectedGraph<String> prerequisiteGraph = new DirectedGraph<>();
    
    private final CustomStack<Command> undoStack = new CustomStack<>();
    private final CustomQueue<Command> redoQueue = new CustomQueue<>();
    private final CustomQueue<ScholarshipApplication> scholarshipQueue = new CustomQueue<>();
    private final CustomStack<Announcement> announcementStack = new CustomStack<>();
    private final CustomQueue<Message> messageQueue = new CustomQueue<>();
    
    private final SQLiteStorage storage = new SQLiteStorage();

    public void loadOrSeed() {
        resetMemory();
        if (storage.hasAnyData()) {
            importSnapshot(storage.loadSnapshot());
            pushActivity("Veriler yerel veri tabanindan yuklendi.");
        } else {
            seed();
            pushActivity("Ilk kurulum verileri olusturuldu.");
        }
    }

    public boolean addStudent(Student student) {
        if (student == null || student.getId().isEmpty() || studentsById.search(student.getId()) != null) {
            return false;
        }
        studentsById.insert(student.getId(), student);
        studentCourseIndex.insert(student.getId(), new DoublyLinkedList<>());
        topStudents.insert(student);
        storage.upsertStudent(student);
        pushActivity("Ogrenci eklendi: " + student.getFullName() + " (" + student.getId() + ")");
        undoStack.push(new Command(Command.Type.ADD_STUDENT, student));
        redoQueue.clear();
        syncActivities();
        return true;
    }

    public boolean addTeacher(Teacher teacher) {
        if (teacher == null || teacher.getId().isEmpty() || teachersById.search(teacher.getId()) != null) {
            return false;
        }
        teachersById.insert(teacher.getId(), teacher);
        storage.upsertTeacher(teacher);
        pushActivity("Ogretim gorevlisi eklendi: " + teacher.getFullName() + " (" + teacher.getDepartment() + ")");
        undoStack.push(new Command(Command.Type.ADD_TEACHER, teacher));
        redoQueue.clear();
        syncActivities();
        return true;
    }

    public boolean addCourse(Course course) {
        if (course == null || course.getCode().isEmpty() || coursesByCode.search(course.getCode()) != null) {
            return false;
        }
        coursesByCode.insert(course.getCode(), course);
        prerequisiteGraph.addVertex(course.getCode());
        storage.upsertCourse(course);
        pushActivity("Ders eklendi: " + course.getCode() + " - " + course.getTitle());
        undoStack.push(new Command(Command.Type.ADD_COURSE, course));
        redoQueue.clear();
        syncActivities();
        return true;
    }

    public boolean deleteStudent(String id) {
        if (studentsById.search(id) == null) return false;
        studentsById.delete(id);
        studentCourseIndex.delete(id);
        storage.deleteStudent(id);
        pushActivity("Ogrenci silindi: " + id);
        syncActivities();
        return true;
    }

    public boolean deleteTeacher(String id) {
        if (teachersById.search(id) == null) return false;
        teachersById.delete(id);
        storage.deleteTeacher(id);
        pushActivity("Ogretim elemani silindi: " + id);
        syncActivities();
        return true;
    }

    public boolean deleteCourse(String code) {
        if (coursesByCode.search(code) == null) return false;
        coursesByCode.delete(code);
        storage.deleteCourse(code);
        pushActivity("Ders silindi: " + code);
        syncActivities();
        return true;
    }

    public boolean enrollStudent(String studentId, String courseCode) {
        if (studentsById.search(studentId) == null || coursesByCode.search(courseCode) == null) {
            return false;
        }
        if (!canEnroll(studentId, courseCode)) {
            return false;
        }
        DoublyLinkedList<String> enrollments = studentCourseIndex.search(studentId);
        if (enrollments == null) {
            enrollments = new DoublyLinkedList<>();
            studentCourseIndex.insert(studentId, enrollments);
        }
        
        for (String code : enrollments) {
            if (code.equals(courseCode)) return false;
        }
        
        enrollments.addLast(courseCode);
        storage.addEnrollment(studentId, courseCode);
        pushActivity("Derse kayit: " + studentId + " -> " + courseCode);
        syncActivities();
        return true;
    }

    public boolean addPrerequisite(String prerequisiteCode, String targetCourseCode) {
        if (coursesByCode.search(prerequisiteCode) == null || coursesByCode.search(targetCourseCode) == null) {
            return false;
        }
        if (prerequisiteCode.equals(targetCourseCode)) {
            return false;
        }
        
        prerequisiteGraph.addEdge(prerequisiteCode, targetCourseCode);
        storage.addPrerequisite(prerequisiteCode, targetCourseCode);
        pushActivity("On kosul eklendi: " + prerequisiteCode + " -> " + targetCourseCode);
        syncActivities();
        return true;
    }

    private void syncActivities() {
        storage.saveActivities(getActivitySnapshot());
    }

    public boolean undoLastOperation() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Command cmd = undoStack.pop();
        switch (cmd.type) {
            case ADD_STUDENT:
                Student s = (Student) cmd.payload;
                studentsById.delete(s.getId());
                studentCourseIndex.delete(s.getId());
                storage.deleteStudent(s.getId());
                break;
            case ADD_TEACHER:
                Teacher t = (Teacher) cmd.payload;
                teachersById.delete(t.getId());
                storage.deleteTeacher(t.getId());
                break;
            case ADD_COURSE:
                Course c = (Course) cmd.payload;
                coursesByCode.delete(c.getCode());
                storage.deleteCourse(c.getCode());
                break;
        }
        redoQueue.enqueue(cmd);
        pushActivity("Geri alindi: " + cmd.type);
        syncActivities();
        return true;
    }

    public boolean redoLastOperation() {
        if (redoQueue.isEmpty()) {
            return false;
        }
        Command cmd = redoQueue.dequeue();
        switch (cmd.type) {
            case ADD_STUDENT:
                Student s = (Student) cmd.payload;
                studentsById.insert(s.getId(), s);
                studentCourseIndex.insert(s.getId(), new DoublyLinkedList<>());
                storage.upsertStudent(s);
                break;
            case ADD_TEACHER:
                Teacher t = (Teacher) cmd.payload;
                teachersById.insert(t.getId(), t);
                storage.upsertTeacher(t);
                break;
            case ADD_COURSE:
                Course c = (Course) cmd.payload;
                coursesByCode.insert(c.getCode(), c);
                storage.upsertCourse(c);
                break;
        }
        undoStack.push(cmd);
        pushActivity("Ileri alindi: " + cmd.type);
        syncActivities();
        return true;
    }

    public String[] buildCoursePlanForStudent(String studentId) {
        if (studentsById.search(studentId) == null) {
            return new String[]{"Ogrenci bulunamadi."};
        }
        
        DoublyLinkedList<String> takenList = studentCourseIndex.search(studentId);
        String[] ordered = prerequisiteGraph.getTopologicalSort(new String[0]);

        DoublyLinkedList<String> resultList = new DoublyLinkedList<>();
        for (String code : ordered) {
            boolean isTaken = false;
            if (takenList != null) {
                for (String taken : takenList) {
                    if (taken.equals(code)) {
                        isTaken = true;
                        break;
                    }
                }
            }
            
            if (!isTaken) {
                Course course = coursesByCode.search(code);
                if (course != null) {
                    resultList.addLast(code + " - " + course.getTitle() + " (" + course.getCredit() + " kredi)");
                }
            }
        }
        
        if (resultList.size() == 0) {
            return new String[]{"Tum dersler tamamlanmis gorunuyor."};
        }
        return resultList.toArray(new String[0]);
    }

    public Student[] getStudents() {
        return studentsById.toArray(new Student[0]);
    }

    public Teacher[] getTeachers() {
        return teachersById.toArray(new Teacher[0]);
    }

    public Course[] getCourses() {
        return coursesByCode.toArray(new Course[0]);
    }

    public String[] getActivitySnapshot() {
        return activityLog.toArray(new String[0]);
    }

    public Student findStudentById(String id) {
        return studentsById.search(id);
    }

    public boolean canEnroll(String studentId, String courseCode) {
        if (studentsById.search(studentId) == null || coursesByCode.search(courseCode) == null) {
            return false;
        }
        DoublyLinkedList<String> prerequisites = prerequisiteGraph.getDirectPrerequisites(courseCode);
        DoublyLinkedList<String> taken = studentCourseIndex.search(studentId);
        if (taken == null) {
            taken = new DoublyLinkedList<>();
        }
        for (String prereq : prerequisites) {
            boolean found = false;
            for (String t : taken) {
                if (t.equals(prereq)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public Student[] getTopStudents(int count) {
        if (count <= 0 || topStudents.isEmpty()) {
            return new Student[0];
        }
        Student[] temp = topStudents.toArray(new Student[0]);
        MaxHeap<Student> copy = new MaxHeap<>(Math.max(count, temp.length), (s1, s2) -> Double.compare(s1.getGpa(), s2.getGpa()));
        for (Student s : temp) {
            copy.insert(s);
        }
        int resultSize = Math.min(count, copy.size());
        Student[] result = new Student[resultSize];
        for (int i = 0; i < resultSize; i++) {
            result[i] = copy.extractMax();
        }
        return result;
    }

    public Message[] getUnreadMessages(String recipient) {
        DoublyLinkedList<Message> unread = new DoublyLinkedList<>();
        Message[] all = messageQueue.toArray(new Message[0]);
        for (Message m : all) {
            if (m.getRecipient().equals(recipient) && !m.isRead()) {
                unread.addLast(m);
            }
        }
        return unread.toArray(new Message[0]);
    }

    public Announcement[] getRecentAnnouncements(int limit) {
        Announcement[] all = announcementStack.toArray(new Announcement[0]);
        int resultSize = Math.min(limit, all.length);
        Announcement[] result = new Announcement[resultSize];
        System.arraycopy(all, 0, result, 0, resultSize);
        return result;
    }

    public int studentCount() {
        return studentsById.size();
    }

    public int teacherCount() {
        return teachersById.size();
    }

    public int courseCount() {
        return coursesByCode.size();
    }

    public Student[] getStudentsSortedByGpa() {
        Student[] students = getStudents();
        MergeSort.sort(students, (s1, s2) -> Double.compare(s2.getGpa(), s1.getGpa())); // Descending
        return students;
    }

    public Student[] getStudentsSortedByName() {
        Student[] students = getStudents();
        QuickSort.sort(students, (s1, s2) -> s1.getFullName().compareToIgnoreCase(s2.getFullName()));
        return students;
    }

    public String topStudentDisplay() {
        Student top = topStudents.peek();
        if (top == null) {
            return "-";
        }
        return top.getFullName() + " (Not Ortalamasi: " + String.format("%.2f", top.getGpa()) + ")";
    }

    public String[] getPrerequisiteEdges() {
        DoublyLinkedList<String> edges = new DoublyLinkedList<>();
        String[] courses = prerequisiteGraph.getAllVertices(new String[0]);
        for (String c1 : courses) {
            for (String c2 : courses) {
                if (prerequisiteGraph.isPrerequisite(c1, c2)) {
                    // Check if it's a direct edge (simplified)
                    // For a more accurate direct edge check, we'd need to look at headEdge
                    edges.addLast(c1 + " -> " + c2);
                }
            }
        }
        return edges.toArray(new String[0]);
    }

    public boolean hasCycleInPrerequisites() {
        // Simplified check using topological sort logic: 
        // if topological sort doesn't include all courses, there's a cycle.
        String[] sorted = prerequisiteGraph.getTopologicalSort(new String[0]);
        return sorted.length < prerequisiteGraph.getVertexCount();
    }

    public void enqueueScholarship(ScholarshipApplication app) {
        scholarshipQueue.enqueue(app);
        pushActivity("Burs basvurusu siraya eklendi: " + app.getStudentName());
    }

    public ScholarshipApplication dequeueScholarship() {
        if (scholarshipQueue.isEmpty()) {
            return null;
        }
        ScholarshipApplication app = scholarshipQueue.dequeue();
        pushActivity("Burs basvurusu isleme alindi: " + app.getStudentName());
        return app;
    }

    public ScholarshipApplication[] scholarshipQueueArray() {
        return scholarshipQueue.toArray(new ScholarshipApplication[0]);
    }

    public void pushAnnouncement(Announcement ann) {
        announcementStack.push(ann);
        pushActivity("Duyuru yayinlandi: " + ann.getTitle());
    }

    public Announcement popAnnouncement() {
        if (announcementStack.isEmpty()) {
            return null;
        }
        Announcement ann = announcementStack.pop();
        pushActivity("Duyuru kaldirildi: " + ann.getTitle());
        return ann;
    }

    public Announcement[] announcementStackArray() {
        return announcementStack.toArray(new Announcement[0]);
    }

    public void sendMessage(Message msg) {
        messageQueue.enqueue(msg);
        pushActivity("Mesaj gonderildi: " + msg.getSubject() + " -> " + msg.getRecipient());
    }

    public Message[] messageArray() {
        return messageQueue.toArray(new Message[0]);
    }

    private void pushActivity(String message) {
        activityLog.addFirst(message);
        if (activityLog.size() > 20) {
            activityLog.removeLast();
        }
    }

    private void seed() {
        addCourse(new Course("CS101", "Programlama Temelleri", 5, "Bilgisayar Mühendisliği", 60, "B-Blok 201", "Pazartesi 10:00-11:50"));
        addCourse(new Course("IT204", "Veri Yapıları", 6, "Bilgisayar Mühendisliği", 55, "B-Blok 202", "Salı 09:00-10:50"));
        addCourse(new Course("SE301", "Yazılım Mimarisi", 4, "Yazılım Mühendisliği", 45, "C-Blok 105", "Çarşamba 13:00-14:50"));
        addCourse(new Course("AI305", "Yapay Zekaya Giriş", 4, "Bilgisayar Mühendisliği", 50, "B-Blok 305", "Perşembe 10:00-11:50"));

        addTeacher(new Teacher("T-100", "Dr. Aylin Kara", "Bilgisayar Mühendisliği", "Doç. Dr.", "aylin.kara@marun.edu.tr", "2015-09-01"));
        addTeacher(new Teacher("T-101", "Doç. Emre Aslan", "Yazılım Mühendisliği", "Dr.", "emre.aslan@marun.edu.tr", "2018-02-15"));

        addStudent(new Student("S-001", "Mert Yıldız", 3.70, "Bahar 2026", "Bilgisayar Mühendisliği", "2023-09-15", 120, "Aktif"));
        addStudent(new Student("S-002", "Elif Demir", 3.92, "Bahar 2026", "Yazılım Mühendisliği", "2022-09-10", 135, "Aktif"));
        addStudent(new Student("S-003", "Deniz Arslan", 3.35, "Güz 2025", "Bilgisayar Mühendisliği", "2024-02-20", 90, "Aktif"));

        enrollStudent("S-001", "CS101");
        enrollStudent("S-001", "IT204");
        enrollStudent("S-002", "SE301");

        addPrerequisite("CS101", "IT204");
        addPrerequisite("IT204", "SE301");
        addPrerequisite("SE301", "AI305");

        enqueueScholarship(new ScholarshipApplication("B-001", "Mert Yıldız", "Bilgisayar Mühendisliği", 3.70, "2026-04-20", "Beklemede"));
        enqueueScholarship(new ScholarshipApplication("B-002", "Elif Demir", "Yazılım Mühendisliği", 3.92, "2026-04-21", "Beklemede"));
        enqueueScholarship(new ScholarshipApplication("B-003", "Deniz Arslan", "Bilgisayar Mühendisliği", 3.35, "2026-04-22", "Beklemede"));

        pushAnnouncement(new Announcement("Bahar Dönemi Kayıtları Başladı", "2026-2027 Bahar dönemi ders kayıtları 15 Mayıs'ta başlayacaktır.", "2026-04-20", "Rektörlük"));
        pushAnnouncement(new Announcement("Kütüphane Çalışma Saatleri", "Final dönemi boyunca kütüphane 24 saat açık olacaktır.", "2026-04-21", "Kütüphane Daire Bşk."));
        pushAnnouncement(new Announcement("Yaz Okulu Duyurusu", "2026 Yaz Okulu başvuruları 1 Haziran'da sona erecektir.", "2026-04-22", "Öğrenci İşleri"));

        sendMessage(new Message("Öğrenci İşleri", "S-001", "Ders Kayıt Onayı", "2026-2027 Bahar dönemi ders kaydınız onaylanmıştır.", "2026-04-20", true));
        sendMessage(new Message("Dr. Aylin Kara", "S-001", "Proje Teslimi", "Proje raporunuzu Pazartesi gününe kadar yükleyiniz.", "2026-04-21", false));
        sendMessage(new Message("S-002", "Dr. Aylin Kara", "Devamsizlik Bilgisi", "Mazeretli devamsizlik dilekcem ektedir.", "2026-04-22", false));

        undoStack.clear();
        redoQueue.clear();
        persist();
    }

    private void resetMemory() {
        studentsById.clear();
        teachersById.clear();
        coursesByCode.clear();
        studentCourseIndex.clear();
        activityLog.clear();
        topStudents.clear();
        prerequisiteGraph.clear();
        undoStack.clear();
        redoQueue.clear();
        scholarshipQueue.clear();
        announcementStack.clear();
        messageQueue.clear();
    }

    private void importSnapshot(SQLiteStorage.Snapshot snapshot) {
        for (Course c : snapshot.courses) {
            coursesByCode.insert(c.getCode(), c);
            prerequisiteGraph.addVertex(c.getCode());
        }
        for (Teacher t : snapshot.teachers) {
            teachersById.insert(t.getId(), t);
        }
        for (Student s : snapshot.students) {
            studentsById.insert(s.getId(), s);
            studentCourseIndex.insert(s.getId(), new DoublyLinkedList<>());
            topStudents.insert(s);
        }
        for (String[] pair : snapshot.enrollments) {
            DoublyLinkedList<String> list = studentCourseIndex.search(pair[0]);
            if (list == null) {
                list = new DoublyLinkedList<>();
                studentCourseIndex.insert(pair[0], list);
            }
            list.addLast(pair[1]);
        }
        for (String[] pair : snapshot.prerequisites) {
            prerequisiteGraph.addEdge(pair[0], pair[1]);
        }
        for (Announcement a : snapshot.announcements) {
            announcementStack.push(a);
        }
        for (Message m : snapshot.messages) {
            messageQueue.enqueue(m);
        }
        for (ScholarshipApplication app : snapshot.scholarshipApplications) {
            scholarshipQueue.enqueue(app);
        }
        activityLog.clear();
        if (!snapshot.activities.isEmpty()) {
            for (int i = snapshot.activities.size() - 1; i >= 0; i--) {
                activityLog.addFirst(snapshot.activities.get(i));
            }
        }
        undoStack.clear();
        redoQueue.clear();
    }

    public SQLiteStorage.LoginRecord findUser(String username) {
        return storage.findUser(username);
    }

    private SQLiteStorage.Snapshot buildSnapshot() {
        SQLiteStorage.Snapshot s = new SQLiteStorage.Snapshot();
        for (Student st : getStudents()) s.students.add(st);
        for (Teacher t : getTeachers()) s.teachers.add(t);
        for (Course c : getCourses()) s.courses.add(c);
        for (Student st : getStudents()) {
            DoublyLinkedList<String> taken = studentCourseIndex.search(st.getId());
            if (taken != null) {
                for (String code : taken) {
                    s.enrollments.add(new String[]{st.getId(), code});
                }
            }
        }
        for (String target : prerequisiteGraph.getAllVertices(new String[0])) {
            for (String prereq : prerequisiteGraph.getDirectPrerequisites(target)) {
                s.prerequisites.add(new String[]{prereq, target});
            }
        }
        for (Announcement a : announcementStackArray()) s.announcements.add(a);
        for (Message m : messageArray()) s.messages.add(m);
        for (ScholarshipApplication app : scholarshipQueueArray()) s.scholarshipApplications.add(app);
        for (String act : getActivitySnapshot()) s.activities.add(act);
        return s;
    }

    private void persist() {
        storage.saveSnapshot(buildSnapshot());
    }

    private static final class Command {
        enum Type { ADD_STUDENT, ADD_TEACHER, ADD_COURSE }
        final Type type;
        final Object payload;
        Command(Type type, Object payload) {
            this.type = type;
            this.payload = payload;
        }
    }
}
