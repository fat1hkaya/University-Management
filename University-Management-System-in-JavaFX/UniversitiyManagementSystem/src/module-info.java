module universite.veri.yapisi.otomasyonu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;
    requires jbcrypt;

    exports universitiymanagementsystem;
    exports universitiymanagementsystem.model;
    exports universitiymanagementsystem.service;
    exports universitiymanagementsystem.repository;
    exports universitiymanagementsystem.viewmodel;
    exports universitiymanagementsystem.controller;
    opens universitiymanagementsystem to javafx.fxml;
    opens universitiymanagementsystem.controller to javafx.fxml;
}
