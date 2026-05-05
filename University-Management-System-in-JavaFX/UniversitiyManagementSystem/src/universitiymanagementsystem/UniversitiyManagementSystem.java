/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package universitiymanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import universitiymanagementsystem.controller.LoginController;
import universitiymanagementsystem.controller.MainController;
import universitiymanagementsystem.service.UniversityService;

/**
 *
 * @author WINDOWS 10
 */
public class UniversitiyManagementSystem extends Application {
    private Stage primaryStage;
    private UniversityService service;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.service = new UniversityService();
        this.service.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/LoginView.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setDependencies(this, service);

        Scene scene = new Scene(root);
        
        stage.setTitle("Universite Veri Yapisi Otomasyonu");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    public void showMainView(String username, String roleName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/MainView.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setDependencies(service, username, roleName);
            // Ekran boyutuna göre makul bir başlangıç boyutu ver
            javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
            double w = Math.min(1280, screen.getWidth() * 0.92);
            double h = Math.min(800, screen.getHeight() * 0.92);
            primaryStage.setScene(new Scene(root, w, h));
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
        } catch (Exception ex) {
            throw new RuntimeException("Ana panel açılırken hata oluştu.", ex);
        }
    }

    @Override
    public void stop() throws Exception {
        if (service != null) {
            service.shutdown();
        }
        super.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
