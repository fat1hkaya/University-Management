package universitiymanagementsystem.controller;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import universitiymanagementsystem.model.Announcement;
import universitiymanagementsystem.model.Course;
import universitiymanagementsystem.model.GpaRecord;
import universitiymanagementsystem.model.Message;
import universitiymanagementsystem.model.ScholarshipApplication;
import universitiymanagementsystem.model.Student;
import universitiymanagementsystem.model.Teacher;
import universitiymanagementsystem.service.UniversityService;
import universitiymanagementsystem.viewmodel.ExamRow;
import universitiymanagementsystem.viewmodel.GradeRow;

public class MainController implements Initializable {

    @FXML private Parent rootContainer;
    @FXML private Label welcomeRoleLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label topStudentLabel;

    @FXML private Button menuDashboardBtn;
    @FXML private Button menuStudentsBtn;
    @FXML private Button menuTeachersBtn;
    @FXML private Button menuCoursesBtn;
    @FXML private Button menuGradesBtn;
    @FXML private Button menuExamsBtn;
    @FXML private Button menuQueueBtn;
    @FXML private Button menuMessagesBtn;
    @FXML private Button menuAnnouncementBtn;

    @FXML private VBox dashboardPane;
    @FXML private VBox studentsPane;
    @FXML private VBox teachersPane;
    @FXML private VBox coursesPane;
    @FXML private VBox gradesPane;
    @FXML private VBox examsPane;
    @FXML private VBox queuePane;
    @FXML private VBox messagesPane;
    @FXML private VBox announcementPane;

    // Dashboard table
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentIdCol;
    @FXML private TableColumn<Student, String> studentNameCol;
    @FXML private TableColumn<Student, String> studentDeptCol;
    @FXML private TableColumn<Student, String> studentRegDateCol;
    @FXML private TableColumn<Student, String> studentEctsCol;
    @FXML private TableColumn<Student, String> studentGpaCol;
    @FXML private TableColumn<Student, String> studentStatusCol;
    @FXML private TableColumn<Student, String> studentActionsCol;

    // Students pane table
    @FXML private TableView<Student> studentTable2;
    @FXML private TableColumn<Student, String> studentIdCol2;
    @FXML private TableColumn<Student, String> studentNameCol2;
    @FXML private TableColumn<Student, String> studentDeptCol2;
    @FXML private TableColumn<Student, String> studentRegDateCol2;
    @FXML private TableColumn<Student, String> studentEctsCol2;
    @FXML private TableColumn<Student, String> studentGpaCol2;
    @FXML private TableColumn<Student, String> studentStatusCol2;
    @FXML private TableColumn<Student, String> studentActionsCol2;

    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, String> teacherIdCol;
    @FXML private TableColumn<Teacher, String> teacherNameCol;
    @FXML private TableColumn<Teacher, String> teacherTitleCol;
    @FXML private TableColumn<Teacher, String> teacherDeptCol;
    @FXML private TableColumn<Teacher, String> teacherEmailCol;
    @FXML private TableColumn<Teacher, String> teacherHireDateCol;
    @FXML private TableColumn<Teacher, String> teacherActionsCol;

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> courseCodeCol;
    @FXML private TableColumn<Course, String> courseTitleCol;
    @FXML private TableColumn<Course, String> courseDeptCol;
    @FXML private TableColumn<Course, String> courseCreditCol;
    @FXML private TableColumn<Course, String> courseEctsCol;
    @FXML private TableColumn<Course, String> courseQuotaCol;
    @FXML private TableColumn<Course, String> courseClassroomCol;
    @FXML private TableColumn<Course, String> courseScheduleCol;
    @FXML private TableColumn<Course, String> courseActionsCol;

    @FXML private TableView<GradeRow> gradeTable;
    @FXML private TableColumn<GradeRow, String> gradeStudentIdCol;
    @FXML private TableColumn<GradeRow, String> gradeStudentNameCol;
    @FXML private TableColumn<GradeRow, String> gradeStudentDeptCol;
    @FXML private TableColumn<GradeRow, String> gradeStudentGpaCol;
    @FXML private TableColumn<GradeRow, String> gradeLetterCol;
    @FXML private TableColumn<GradeRow, String> gradeStatusCol;

    @FXML private TextField studentIdInput;
    @FXML private TextField studentNameInput;
    @FXML private TextField studentDeptInput;
    @FXML private TextField studentGpaInput;
    @FXML private TextField studentSemesterInput;
    @FXML private TextField studentEctsInput;
    @FXML private Label studentFormHeader;
    @FXML private Button studentActionBtn;

    @FXML private TextField teacherIdInput;
    @FXML private TextField teacherNameInput;
    @FXML private TextField teacherTitleInput;
    @FXML private TextField teacherDepartmentInput;
    @FXML private TextField teacherEmailInput;
    @FXML private Label teacherFormHeader;
    @FXML private Button teacherActionBtn;

    @FXML private TextField courseCodeInput;
    @FXML private TextField courseTitleInput;
    @FXML private TextField courseCreditInput;
    @FXML private TextField courseDeptInput;
    @FXML private TextField courseQuotaInput;
    @FXML private Label courseFormHeader;
    @FXML private Button courseActionBtn;

    @FXML private TableView<ExamRow> examTable;
    @FXML private TableColumn<ExamRow, String> examCourseCol;
    @FXML private TableColumn<ExamRow, String> examDateCol;
    @FXML private TableColumn<ExamRow, String> examPriorityCol;
    @FXML private TableColumn<ExamRow, String> examTypeCol;

    @FXML private TableView<ScholarshipApplication> scholarshipTable;
    @FXML private TableColumn<ScholarshipApplication, String> scholarshipIdCol;
    @FXML private TableColumn<ScholarshipApplication, String> scholarshipNameCol;
    @FXML private TableColumn<ScholarshipApplication, String> scholarshipDeptCol;
    @FXML private TableColumn<ScholarshipApplication, String> scholarshipGpaCol;
    @FXML private TableColumn<ScholarshipApplication, String> scholarshipDateCol;
    @FXML private TableColumn<ScholarshipApplication, String> scholarshipStatusCol;

    @FXML private TextField scholarshipIdInput;
    @FXML private TextField scholarshipNameInput;
    @FXML private TextField scholarshipDeptInput;
    @FXML private TextField scholarshipGpaInput;

    @FXML private TableView<Announcement> announcementTable;
    @FXML private TableColumn<Announcement, String> announcementTitleCol;
    @FXML private TableColumn<Announcement, String> announcementContentCol;
    @FXML private TableColumn<Announcement, String> announcementDateCol;
    @FXML private TableColumn<Announcement, String> announcementAuthorCol;

    @FXML private TextField announcementTitleInput;
    @FXML private TextField announcementContentInput;
    @FXML private TextField announcementAuthorInput;

    @FXML private TableView<Message> messageTable;
    @FXML private TableColumn<Message, String> msgSenderCol;
    @FXML private TableColumn<Message, String> msgRecipientCol;
    @FXML private TableColumn<Message, String> msgSubjectCol;
    @FXML private TableColumn<Message, String> msgContentCol;
    @FXML private TableColumn<Message, String> msgDateCol;
    @FXML private TableColumn<Message, String> msgReadCol;

    @FXML private LineChart<String, Number> gpaChart;
    @FXML private VBox timelineContainer;
    @FXML private HBox actionCardsContainer;

    @FXML private TextField msgSenderInput;
    @FXML private TextField msgRecipientInput;
    @FXML private TextField msgSubjectInput;
    @FXML private TextField msgContentInput;

    private UniversityService service;
    private ObservableList<Student> allStudents = FXCollections.observableArrayList();
    private ObservableList<Teacher> allTeachers = FXCollections.observableArrayList();
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();

    private String editingStudentId = null;
    private String editingTeacherId = null;
    private String editingCourseCode = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStudentColumns(studentIdCol, studentNameCol, studentDeptCol, studentRegDateCol,
                studentEctsCol, studentGpaCol, studentStatusCol, studentActionsCol);
        setupStudentColumns(studentIdCol2, studentNameCol2, studentDeptCol2, studentRegDateCol2,
                studentEctsCol2, studentGpaCol2, studentStatusCol2, studentActionsCol2);
        addActionColumn(studentActionsCol, this::editStudent, this::deleteStudent);
        addActionColumn(studentActionsCol2, this::editStudent, this::deleteStudent);

        teacherIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        teacherNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        teacherTitleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        teacherDeptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment()));
        teacherEmailCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        teacherHireDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHireDate()));
        addActionColumn(teacherActionsCol, this::editTeacher, this::deleteTeacher);

        courseCodeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        courseTitleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        courseDeptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment()));
        courseCreditCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCredit())));
        courseEctsCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCredit() * 2)));
        courseQuotaCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuota())));
        courseClassroomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClassroom()));
        courseScheduleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSchedule()));
        addActionColumn(courseActionsCol, this::editCourse, this::deleteCourse);

        gradeStudentIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentId));
        gradeStudentNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentName));
        gradeStudentDeptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().department));
        gradeStudentGpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().gpa)));
        gradeLetterCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().letterGrade));
        gradeStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        examCourseCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().course));
        examDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date));
        examPriorityCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().priority)));
        examTypeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().type));

        scholarshipIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        scholarshipNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));
        scholarshipDeptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment()));
        scholarshipGpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getGpa())));
        scholarshipDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApplicationDate()));
        scholarshipStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        announcementTitleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        announcementContentCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContent()));
        announcementDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate()));
        announcementAuthorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));

        msgSenderCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSender()));
        msgRecipientCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRecipient()));
        msgSubjectCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSubject()));
        msgContentCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContent()));
        msgDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate()));
        msgReadCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isRead() ? "Okundu" : "Yeni"));

        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentTable2.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        teacherTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        gradeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        examTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scholarshipTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        announcementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        messageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupBadgeColumns();
        setupClickableRows();
        buildTimeline();
        buildActionCards();

        showDashboard();
    }

    private void setupStudentColumns(TableColumn<Student, String> idCol, TableColumn<Student, String> nameCol,
                                     TableColumn<Student, String> deptCol, TableColumn<Student, String> regDateCol,
                                     TableColumn<Student, String> ectsCol, TableColumn<Student, String> gpaCol,
                                     TableColumn<Student, String> statusCol, TableColumn<Student, String> actionsCol) {
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        deptCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment()));
        regDateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegistrationDate()));
        ectsCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getEcts())));
        gpaCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getGpa())));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
    }

    private <T> void addActionColumn(TableColumn<T, String> col, java.util.function.Consumer<T> onEdit, java.util.function.Consumer<T> onDelete) {
        col.setCellFactory(c -> new TableCell<T, String>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("✕");
            private final HBox box = new HBox(6, editBtn, deleteBtn);
            {
                box.setPadding(new Insets(0));
                editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 2 6;");
                editBtn.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> onDelete.accept(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    public void setDependencies(UniversityService service, String username, String roleName) {
        this.service = service;
        this.welcomeRoleLabel.setText("Kullanıcı: " + username + " | Yetki: " + roleName);
        refreshAll();
        refreshGpaChart();
    }

    @FXML private void showDashboard() {
        setActivePane(dashboardPane, "Ana Sayfa > Genel Bakış");
        setActiveMenu(menuDashboardBtn);
    }
    @FXML private void showStudents() {
        setActivePane(studentsPane, "Ana Sayfa > Öğrenci İşleri");
        setActiveMenu(menuStudentsBtn);
    }
    @FXML private void showTeachers() {
        setActivePane(teachersPane, "Ana Sayfa > Öğretim Elemanları");
        setActiveMenu(menuTeachersBtn);
    }
    @FXML private void showCourses() {
        setActivePane(coursesPane, "Ana Sayfa > Ders Kayıt Sistemi");
        setActiveMenu(menuCoursesBtn);
    }
    @FXML private void showGrades() {
        setActivePane(gradesPane, "Ana Sayfa > Not İşlemleri");
        setActiveMenu(menuGradesBtn);
        refreshGrades();
    }
    @FXML private void showExams() {
        setActivePane(examsPane, "Ana Sayfa > Sınav Planlayıcı");
        setActiveMenu(menuExamsBtn);
        refreshExams();
    }
    @FXML private void showQueue() {
        setActivePane(queuePane, "Ana Sayfa > Burs ve Yardım Başvuruları");
        setActiveMenu(menuQueueBtn);
        refreshQueue();
    }
    @FXML private void showMessages() {
        setActivePane(messagesPane, "Ana Sayfa > Mesaj Kutusu");
        setActiveMenu(menuMessagesBtn);
        refreshMessages();
    }
    @FXML private void showAnnouncements() {
        setActivePane(announcementPane, "Ana Sayfa > Duyuru Panosu");
        setActiveMenu(menuAnnouncementBtn);
        refreshAnnouncements();
    }

    @FXML
    private void addStudent() {
        if (editingStudentId != null) {
            runAsyncAction(() -> {
                service.deleteStudent(editingStudentId);
                Student s = new Student(
                        studentIdInput.getText().trim(),
                        studentNameInput.getText().trim(),
                        Double.parseDouble(studentGpaInput.getText().trim()),
                        studentSemesterInput.getText().trim(),
                        studentDeptInput.getText().trim(),
                        "2026-04-22",
                        Integer.parseInt(studentEctsInput.getText().trim()),
                        "Aktif"
                );
                return service.addStudent(s) ? "Öğrenci güncellendi." : "Öğrenci güncellenemedi.";
            }, "Güncelleme başarısız.", () -> { clearStudentInputs(); resetStudentEdit(); });
        } else {
            runAsyncAction(() -> {
                Student s = new Student(
                        studentIdInput.getText().trim(),
                        studentNameInput.getText().trim(),
                        Double.parseDouble(studentGpaInput.getText().trim()),
                        studentSemesterInput.getText().trim(),
                        studentDeptInput.getText().trim(),
                        "2026-04-22",
                        Integer.parseInt(studentEctsInput.getText().trim()),
                        "Aktif"
                );
                return service.addStudent(s) ? "Öğrenci başarıyla eklendi." : "Öğrenci eklenemedi. Numara zaten kayıtlı olabilir.";
            }, "Öğrenci eklenemedi. Ortalama sayısal olmalıdır.", this::clearStudentInputs);
        }
    }

    private void editStudent(Student s) {
        editingStudentId = s.getId();
        studentIdInput.setText(s.getId());
        studentNameInput.setText(s.getFullName());
        studentDeptInput.setText(s.getDepartment());
        studentGpaInput.setText(String.valueOf(s.getGpa()));
        studentSemesterInput.setText(s.getSemester());
        studentEctsInput.setText(String.valueOf(s.getEcts()));
        studentFormHeader.setText("Öğrenci Düzenle");
        studentActionBtn.setText("Güncelle");
        showStudents();
    }

    private void deleteStudent(Student s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, s.getFullName() + " silinecek. Emin misiniz?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Silme Onayı");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                runAsyncAction(() -> service.deleteStudent(s.getId()) ? "Öğrenci silindi." : "Silme başarısız.",
                        "Silme başarısız.", this::refreshAll);
            }
        });
    }

    private void resetStudentEdit() {
        editingStudentId = null;
        studentFormHeader.setText("Yeni Öğrenci Ekle");
        studentActionBtn.setText("Ekle");
    }

    @FXML
    private void addTeacher() {
        if (editingTeacherId != null) {
            runAsyncAction(() -> {
                service.deleteTeacher(editingTeacherId);
                Teacher t = new Teacher(
                        teacherIdInput.getText().trim(),
                        teacherNameInput.getText().trim(),
                        teacherDepartmentInput.getText().trim(),
                        teacherTitleInput.getText().trim(),
                        teacherEmailInput.getText().trim(),
                        "2026-04-22"
                );
                return service.addTeacher(t) ? "Öğretim elemanı güncellendi." : "Güncelleme başarısız.";
            }, "Güncelleme başarısız.", () -> { clearTeacherInputs(); resetTeacherEdit(); });
        } else {
            runAsyncAction(() -> {
                Teacher t = new Teacher(
                        teacherIdInput.getText().trim(),
                        teacherNameInput.getText().trim(),
                        teacherDepartmentInput.getText().trim(),
                        teacherTitleInput.getText().trim(),
                        teacherEmailInput.getText().trim(),
                        "2026-04-22"
                );
                return service.addTeacher(t) ? "Öğretim elemanı başarıyla eklendi." : "Öğretim elemanı eklenemedi.";
            }, "Öğretim elemanı eklenemedi.", this::clearTeacherInputs);
        }
    }

    private void editTeacher(Teacher t) {
        editingTeacherId = t.getId();
        teacherIdInput.setText(t.getId());
        teacherNameInput.setText(t.getFullName());
        teacherDepartmentInput.setText(t.getDepartment());
        teacherTitleInput.setText(t.getTitle());
        teacherEmailInput.setText(t.getEmail());
        teacherFormHeader.setText("Öğretim Elemanı Düzenle");
        teacherActionBtn.setText("Güncelle");
        showTeachers();
    }

    private void deleteTeacher(Teacher t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, t.getFullName() + " silinecek. Emin misiniz?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Silme Onayı");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                runAsyncAction(() -> service.deleteTeacher(t.getId()) ? "Öğretim elemanı silindi." : "Silme başarısız.",
                        "Silme başarısız.", this::refreshAll);
            }
        });
    }

    private void resetTeacherEdit() {
        editingTeacherId = null;
        teacherFormHeader.setText("Yeni Öğretim Elemanı Ekle");
        teacherActionBtn.setText("Ekle");
    }

    @FXML
    private void addCourse() {
        if (editingCourseCode != null) {
            runAsyncAction(() -> {
                service.deleteCourse(editingCourseCode);
                Course c = new Course(
                        courseCodeInput.getText().trim(),
                        courseTitleInput.getText().trim(),
                        Integer.parseInt(courseCreditInput.getText().trim()),
                        courseDeptInput.getText().trim(),
                        Integer.parseInt(courseQuotaInput.getText().trim()),
                        "B-Blok 201",
                        "Belirlenmedi"
                );
                return service.addCourse(c) ? "Ders güncellendi." : "Güncelleme başarısız.";
            }, "Güncelleme başarısız.", () -> { clearCourseInputs(); resetCourseEdit(); });
        } else {
            runAsyncAction(() -> {
                Course c = new Course(
                        courseCodeInput.getText().trim(),
                        courseTitleInput.getText().trim(),
                        Integer.parseInt(courseCreditInput.getText().trim()),
                        courseDeptInput.getText().trim(),
                        Integer.parseInt(courseQuotaInput.getText().trim()),
                        "B-Blok 201",
                        "Belirlenmedi"
                );
                return service.addCourse(c) ? "Ders başarıyla eklendi." : "Ders eklenemedi. Kod zaten kayıtlı olabilir.";
            }, "Ders eklenemedi. Kredi alanı sayısal olmalıdır.", this::clearCourseInputs);
        }
    }

    private void editCourse(Course c) {
        editingCourseCode = c.getCode();
        courseCodeInput.setText(c.getCode());
        courseTitleInput.setText(c.getTitle());
        courseCreditInput.setText(String.valueOf(c.getCredit()));
        courseDeptInput.setText(c.getDepartment());
        courseQuotaInput.setText(String.valueOf(c.getQuota()));
        courseFormHeader.setText("Ders Düzenle");
        courseActionBtn.setText("Güncelle");
        showCourses();
    }

    private void deleteCourse(Course c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, c.getTitle() + " silinecek. Emin misiniz?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Silme Onayı");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                runAsyncAction(() -> service.deleteCourse(c.getCode()) ? "Ders silindi." : "Silme başarısız.",
                        "Silme başarısız.", this::refreshAll);
            }
        });
    }

    private void resetCourseEdit() {
        editingCourseCode = null;
        courseFormHeader.setText("Yeni Ders Ekle");
        courseActionBtn.setText("Ekle");
    }

    @FXML
    private void sortStudentsByGpa() {
        Task<Student[]> task = new Task<Student[]>() {
            @Override protected Student[] call() { return service.studentsSortedByGpa(); }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allStudents.setAll(task.getValue());
            studentTable.setItems(allStudents);
            studentTable2.setItems(allStudents);
        }));
        new Thread(task, "sort-gpa-task").start();
    }

    @FXML
    private void sortStudentsByName() {
        Task<Student[]> task = new Task<Student[]>() {
            @Override protected Student[] call() { return service.studentsSortedByName(); }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allStudents.setAll(task.getValue());
            studentTable.setItems(allStudents);
            studentTable2.setItems(allStudents);
        }));
        new Thread(task, "sort-name-task").start();
    }

    @FXML private void undoAction() {
        runAsyncAction(() -> service.undo() ? "İşlem geri alındı." : "Geri alınacak işlem yok.", "Geri alma başarısız.", this::refreshAll);
    }
    @FXML private void redoAction() {
        runAsyncAction(() -> service.redo() ? "İşlem ileri alındı." : "İleri alınacak işlem yok.", "İleri alma başarısız.", this::refreshAll);
    }

    @FXML
    private void addScholarship() {
        runAsyncAction(() -> {
            ScholarshipApplication app = new ScholarshipApplication(
                    scholarshipIdInput.getText().trim(),
                    scholarshipNameInput.getText().trim(),
                    scholarshipDeptInput.getText().trim(),
                    Double.parseDouble(scholarshipGpaInput.getText().trim()),
                    "2026-04-22",
                    "Beklemede"
            );
            service.enqueueScholarship(app);
            return "Burs başvurusu sıraya eklendi.";
        }, "Burs başvurusu eklenemedi. GPA sayısal olmalıdır.", () -> { clearScholarshipInputs(); refreshQueue(); });
    }

    @FXML
    private void processScholarship() {
        runAsyncAction(() -> {
            ScholarshipApplication app = service.dequeueScholarship();
            return app != null ? "Başvuru işleme alındı: " + app.getStudentName() : "Sırada bekleyen başvuru yok.";
        }, "İşlem başarısız.", this::refreshQueue);
    }

    @FXML
    private void pushAnnouncement() {
        runAsyncAction(() -> {
            Announcement ann = new Announcement(
                    announcementTitleInput.getText().trim(),
                    announcementContentInput.getText().trim(),
                    "2026-04-22",
                    announcementAuthorInput.getText().trim()
            );
            service.pushAnnouncement(ann);
            return "Duyuru yayınlandı.";
        }, "Duyuru yayınlanamadı.", () -> { clearAnnouncementInputs(); refreshAnnouncements(); });
    }

    @FXML
    private void popAnnouncement() {
        runAsyncAction(() -> {
            Announcement ann = service.popAnnouncement();
            return ann != null ? "Duyuru kaldırıldı: " + ann.getTitle() : "Kaldırılacak duyuru yok.";
        }, "İşlem başarısız.", this::refreshAnnouncements);
    }

    @FXML
    private void sendMessage() {
        runAsyncAction(() -> {
            Message msg = new Message(
                    msgSenderInput.getText().trim(),
                    msgRecipientInput.getText().trim(),
                    msgSubjectInput.getText().trim(),
                    msgContentInput.getText().trim(),
                    "2026-04-22",
                    false
            );
            service.sendMessage(msg);
            return "Mesaj gönderildi.";
        }, "Mesaj gönderilemedi.", () -> { clearMessageInputs(); refreshMessages(); });
    }

    private void refreshExams() {
        if (service == null) return;
        ObservableList<ExamRow> exams = FXCollections.observableArrayList();
        exams.add(new ExamRow("CS101 - Programlama Temelleri", "2026-05-15", 95, "Vize"));
        exams.add(new ExamRow("IT204 - Veri Yapıları", "2026-05-18", 90, "Vize"));
        exams.add(new ExamRow("SE301 - Yazılım Mimarisi", "2026-05-20", 85, "Vize"));
        exams.add(new ExamRow("AI305 - Yapay Zekaya Giriş", "2026-05-22", 88, "Vize"));
        exams.sort((e1, e2) -> Integer.compare(e2.priority, e1.priority));
        examTable.setItems(exams);
    }

    private void refreshQueue() {
        if (service == null) return;
        scholarshipTable.setItems(FXCollections.observableArrayList(service.scholarshipQueue()));
    }

    private void refreshAnnouncements() {
        if (service == null) return;
        announcementTable.setItems(FXCollections.observableArrayList(service.announcementStack()));
    }

    private void refreshMessages() {
        if (service == null) return;
        messageTable.setItems(FXCollections.observableArrayList(service.messages()));
    }

    private void refreshGrades() {
        if (service == null) return;
        ObservableList<GradeRow> rows = FXCollections.observableArrayList();
        for (Student s : service.students()) {
            rows.add(new GradeRow(s.getId(), s.getFullName(), s.getDepartment(), s.getGpa(),
                    letterGrade(s.getGpa()), s.getGpa() >= 2.0 ? "Başarılı" : "Başarısız"));
        }
        gradeTable.setItems(rows);
    }

    private String letterGrade(double gpa) {
        if (gpa >= 3.5) return "AA";
        if (gpa >= 3.0) return "BA";
        if (gpa >= 2.5) return "BB";
        if (gpa >= 2.0) return "CB";
        if (gpa >= 1.5) return "CC";
        if (gpa >= 1.0) return "DC";
        return "FF";
    }

    private void refreshAll() {
        allStudents.setAll(service.students());
        allTeachers.setAll(service.teachers());
        allCourses.setAll(service.courses());
        studentTable.setItems(allStudents);
        studentTable2.setItems(allStudents);
        teacherTable.setItems(allTeachers);
        courseTable.setItems(allCourses);

        totalStudentsLabel.setText(String.valueOf(service.studentCount()));
        totalTeachersLabel.setText(String.valueOf(service.teacherCount()));
        totalCoursesLabel.setText(String.valueOf(service.courseCount()));
        topStudentLabel.setText(service.topStudentDisplay());
    }

    private void clearStudentInputs() {
        studentIdInput.clear();
        studentNameInput.clear();
        studentDeptInput.clear();
        studentGpaInput.clear();
        studentSemesterInput.clear();
        studentEctsInput.clear();
    }
    private void clearTeacherInputs() {
        teacherIdInput.clear();
        teacherNameInput.clear();
        teacherTitleInput.clear();
        teacherDepartmentInput.clear();
        teacherEmailInput.clear();
    }
    private void clearCourseInputs() {
        courseCodeInput.clear();
        courseTitleInput.clear();
        courseCreditInput.clear();
        courseDeptInput.clear();
        courseQuotaInput.clear();
    }
    private void clearScholarshipInputs() {
        scholarshipIdInput.clear();
        scholarshipNameInput.clear();
        scholarshipDeptInput.clear();
        scholarshipGpaInput.clear();
    }
    private void clearAnnouncementInputs() {
        announcementTitleInput.clear();
        announcementContentInput.clear();
        announcementAuthorInput.clear();
    }
    private void clearMessageInputs() {
        msgSenderInput.clear();
        msgRecipientInput.clear();
        msgSubjectInput.clear();
        msgContentInput.clear();
    }

    private void runAsyncAction(ActionSupplier supplier, String errorMessage, Runnable onSuccessCleanup) {
        Task<String> task = new Task<String>() {
            @Override protected String call() throws Exception { return supplier.get(); }
        };
        task.setOnSucceeded(e -> {
            if (onSuccessCleanup != null) onSuccessCleanup.run();
            refreshAll();
        });
        new Thread(task, "db-action-task").start();
    }

    @FunctionalInterface
    private interface ActionSupplier {
        String get() throws Exception;
    }

    private void setActivePane(VBox pane, String breadcrumb) {
        dashboardPane.setVisible(false); dashboardPane.setManaged(false);
        studentsPane.setVisible(false); studentsPane.setManaged(false);
        teachersPane.setVisible(false); teachersPane.setManaged(false);
        coursesPane.setVisible(false); coursesPane.setManaged(false);
        gradesPane.setVisible(false); gradesPane.setManaged(false);
        examsPane.setVisible(false); examsPane.setManaged(false);
        queuePane.setVisible(false); queuePane.setManaged(false);
        messagesPane.setVisible(false); messagesPane.setManaged(false);
        announcementPane.setVisible(false); announcementPane.setManaged(false);

        pane.setVisible(true);
        pane.setManaged(true);
        breadcrumbLabel.setText(breadcrumb);
        animateTransition(pane);
    }

    private void setActiveMenu(Button active) {
        menuDashboardBtn.getStyleClass().remove("menu-btn-active");
        menuStudentsBtn.getStyleClass().remove("menu-btn-active");
        menuTeachersBtn.getStyleClass().remove("menu-btn-active");
        menuCoursesBtn.getStyleClass().remove("menu-btn-active");
        menuGradesBtn.getStyleClass().remove("menu-btn-active");
        menuExamsBtn.getStyleClass().remove("menu-btn-active");
        menuQueueBtn.getStyleClass().remove("menu-btn-active");
        menuMessagesBtn.getStyleClass().remove("menu-btn-active");
        menuAnnouncementBtn.getStyleClass().remove("menu-btn-active");
        if (!active.getStyleClass().contains("menu-btn-active")) {
            active.getStyleClass().add("menu-btn-active");
        }
    }

    private void animateTransition(Parent node) {
        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), node);
        slide.setFromY(8.0);
        slide.setToY(0.0);
        fade.play();
        slide.play();
    }

    @FXML
    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Güvenli çıkış yapmak istiyor musunuz?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Çıkış Onayı");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) System.exit(0);
        });
    }

    /* ============================================================
       BADGE STYLING
       ============================================================ */
    private void setupBadgeColumns() {
        setupBadgeColumn(studentStatusCol, "Aktif", "badge-success", "Pasif", "badge-neutral");
        setupBadgeColumn(studentStatusCol2, "Aktif", "badge-success", "Pasif", "badge-neutral");
        setupBadgeColumn(gradeStatusCol, "Başarılı", "badge-success", "Başarısız", "badge-danger");
        setupBadgeColumn(scholarshipStatusCol, "Onaylandı", "badge-success", "Beklemede", "badge-warning");
        setupBadgeColumn(msgReadCol, "Okundu", "badge-neutral", "Yeni", "badge-info");
    }

    private <T> void setupBadgeColumn(TableColumn<T, String> col, String match1, String style1, String match2, String style2) {
        col.setCellFactory(c -> new TableCell<T, String>() {
            private final Label badge = new Label();
            {
                badge.setStyle("-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                if (item.equalsIgnoreCase(match1)) {
                    badge.setStyle("-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #D1FAE5; -fx-text-fill: #065F46;");
                } else if (item.equalsIgnoreCase(match2)) {
                    badge.setStyle("-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: " + (style2.contains("danger") ? "#FEE2E2; -fx-text-fill: #991B1B;" : style2.contains("warning") ? "#FEF3C7; -fx-text-fill: #92400E;" : style2.contains("info") ? "#DBEAFE; -fx-text-fill: #1E40AF;" : "#F1F5F9; -fx-text-fill: #475569;"));
                } else {
                    badge.setStyle("-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #F1F5F9; -fx-text-fill: #475569;");
                }
                setGraphic(badge);
            }
        });
    }

    /* ============================================================
       CLICKABLE ROWS
       ============================================================ */
    private void setupClickableRows() {
        addRowClickHandler(studentTable, s -> editStudent(s));
        addRowClickHandler(studentTable2, s -> editStudent(s));
        addRowClickHandler(teacherTable, t -> editTeacher(t));
        addRowClickHandler(courseTable, c -> editCourse(c));
    }

    private <T> void addRowClickHandler(TableView<T> table, java.util.function.Consumer<T> onClick) {
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    onClick.accept(row.getItem());
                }
            });
            return row;
        });
    }

    /* ============================================================
       TIMELINE WIDGET
       ============================================================ */
    private void buildTimeline() {
        timelineContainer.getChildren().clear();
        String[][] items = {
            {"08:30", "BLM2006.2 - Bilgisayar Ağlarına Giriş", "Recep Tayyip Erdoğan Maltepe Kül. RTE.T2.Z01", "completed"},
            {"10:30", "BLM2008.2 - Mikroişlemciler", "Recep Tayyip Erdoğan Maltepe Kül. RTE.T2.Z07", "completed"},
            {"13:00", "BLM2010.2 - Elektronik Devrelere Giriş", "Recep Tayyip Erdoğan Maltepe Kül. RTE.T2.Z01", "pending"},
            {"15:00", "BLM3062.1 - Açık Kaynak Kodlu Yazılımlar", "Recep Tayyip Erdoğan Maltepe Kül. RTE.T2.Z08", "pending"}
        };
        for (int i = 0; i < items.length; i++) {
            String[] it = items[i];
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 0, 8, 0));

            VBox left = new VBox(4);
            left.setAlignment(Pos.CENTER);
            left.setMinWidth(28);
            Circle dot = new Circle(6);
            if (it[3].equals("completed")) {
                dot.setFill(javafx.scene.paint.Color.web("#10B981"));
                dot.setStroke(javafx.scene.paint.Color.web("#D1FAE5"));
            } else {
                dot.setFill(javafx.scene.paint.Color.web("#2563EB"));
                dot.setStroke(javafx.scene.paint.Color.web("#DBEAFE"));
            }
            dot.setStrokeWidth(3);
            left.getChildren().add(dot);
            if (i < items.length - 1) {
                Line line = new Line(0, 0, 0, 32);
                line.setStroke(javafx.scene.paint.Color.web("#E2E8F0"));
                line.setStrokeWidth(2);
                left.getChildren().add(line);
            }

            VBox content = new VBox(3);
            content.setAlignment(Pos.CENTER_LEFT);
            Label timeLbl = new Label(it[0]);
            timeLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: 600; -fx-min-width: 50px;");
            Label titleLbl = new Label(it[1]);
            titleLbl.setStyle("-fx-text-fill: #0F2744; -fx-font-size: 14px; -fx-font-weight: 600;");
            Label subLbl = new Label(it[2]);
            subLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            top.getChildren().addAll(timeLbl, titleLbl);
            content.getChildren().addAll(top, subLbl);

            row.getChildren().addAll(left, content);
            timelineContainer.getChildren().add(row);
        }
    }

    /* ============================================================
       ACTION CARDS
       ============================================================ */
    private void buildActionCards() {
        actionCardsContainer.getChildren().clear();
        String[][] cards = {
            {"Burs Başvurusu Onayı", "3 yeni burs başvurusu değerlendirme bekliyor.", "Bugün, 14:00", "warning"},
            {"Sınav Programı Onay", "2026 Bahar dönemi sınav programı yayınlandı.", "Dün", "info"},
            {"Ders Kayıt Uyarısı", "12 öğrenci zorunlu ders seçimi tamamlamadı.", "2 gün önce", "danger"},
            {"Akademik Takvim", "Ara sınav dönemi başlangıcı: 12 Mayıs 2026", "3 gün önce", "neutral"}
        };
        for (String[] c : cards) {
            VBox card = new VBox(8);
            card.setPadding(new Insets(16));
            card.setMinWidth(240);
            card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-cursor: hand;");
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setRadius(12);
            shadow.setOffsetX(0);
            shadow.setOffsetY(2);
            shadow.setColor(javafx.scene.paint.Color.web("rgba(15,39,68,0.06)"));
            card.setEffect(shadow);
            card.setOnMouseEntered(e -> {
                javafx.scene.effect.DropShadow hover = new javafx.scene.effect.DropShadow();
                hover.setRadius(16);
                hover.setOffsetX(0);
                hover.setOffsetY(4);
                hover.setColor(javafx.scene.paint.Color.web("rgba(15,39,68,0.10)"));
                card.setEffect(hover);
            });
            card.setOnMouseExited(e -> {
                javafx.scene.effect.DropShadow out = new javafx.scene.effect.DropShadow();
                out.setRadius(12);
                out.setOffsetX(0);
                out.setOffsetY(2);
                out.setColor(javafx.scene.paint.Color.web("rgba(15,39,68,0.06)"));
                card.setEffect(out);
            });

            Label title = new Label(c[0]);
            title.setStyle("-fx-text-fill: #0F2744; -fx-font-size: 14px; -fx-font-weight: 600;");
            Label desc = new Label(c[1]);
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
            Label meta = new Label(c[2]);
            meta.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

            String badgeColor = c[3].equals("warning") ? "#FEF3C7; -fx-text-fill: #92400E;"
                : c[3].equals("info") ? "#DBEAFE; -fx-text-fill: #1E40AF;"
                : c[3].equals("danger") ? "#FEE2E2; -fx-text-fill: #991B1B;"
                : "#F1F5F9; -fx-text-fill: #475569;";
            Label badge = new Label(c[3].equals("warning") ? "Beklemede" : c[3].equals("info") ? "Yeni" : c[3].equals("danger") ? "Acil" : "Bilgi");
            badge.setStyle("-fx-background-radius: 20px; -fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700; -fx-background-color: " + badgeColor);

            HBox top = new HBox(8);
            top.setAlignment(Pos.CENTER_LEFT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            top.getChildren().addAll(badge, spacer);

            card.getChildren().addAll(top, title, desc, meta);
            actionCardsContainer.getChildren().add(card);
        }
    }

    /* ============================================================
       GPA CHART
       ============================================================ */
    private void refreshGpaChart() {
        if (gpaChart == null || service == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ortalama GPA");
        GpaRecord[] history = service.getGpaHistory();
        for (GpaRecord r : history) {
            series.getData().add(new XYChart.Data<>(r.getSemester(), r.getGpa()));
        }
        gpaChart.getData().clear();
        gpaChart.getData().add(series);
        gpaChart.lookupAll(".chart-series-line").forEach(node ->
            node.setStyle("-fx-stroke: #2563EB; -fx-stroke-width: 2.5px;")
        );
        gpaChart.lookupAll(".chart-line-symbol").forEach(node ->
            node.setStyle("-fx-background-color: #2563EB, #FFFFFF; -fx-background-radius: 4px; -fx-padding: 4px;")
        );
    }

}
