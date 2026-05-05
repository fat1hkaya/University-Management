package universitiymanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import universitiymanagementsystem.UniversitiyManagementSystem;
import universitiymanagementsystem.service.UniversityService;
import universitiymanagementsystem.service.ValidationUtil;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    private UniversitiyManagementSystem app;
    private UniversityService service;

    public void setDependencies(UniversitiyManagementSystem app, UniversityService service) {
        this.app = app;
        this.service = service;
    }

    @FXML
    private void signIn() {
        String username = usernameField.getText().trim().toLowerCase();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Lütfen kullanıcı adı ve şifre giriniz.");
            return;
        }
        if (!ValidationUtil.validId(username)) {
            statusLabel.setText("Kullanıcı adı formatı geçersiz.");
            return;
        }
        if (!service.login(username, password)) {
            statusLabel.setText("Giriş başarısız. Bilgilerinizi kontrol ediniz.");
            return;
        }
        app.showMainView(username, service.roleOf(username));
    }

    @FXML
    private void signInEdevlet() {
        statusLabel.setText("e-Devlet entegrasyonu simülasyon modunda çalışıyor.");
    }

    @FXML
    private void forgotPassword() {
        statusLabel.setText("Lütfen sistem yöneticisi ile iletişime geçiniz.");
    }
}
