module org.example.salsiaopf {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires jbcrypt;
    requires jasperreports;
    requires org.eclipse.angus.mail;
    requires jakarta.mail;
    requires java.desktop;

    opens org.example.salsiaopf to javafx.fxml;
    opens org.example.salsiaopf.controller to javafx.fxml;

    exports org.example.salsiaopf;
    exports org.example.salsiaopf.controller;
    exports org.example.salsiaopf.dao;
    exports org.example.salsiaopf.database;
    exports org.example.salsiaopf.model;
    exports org.example.salsiaopf.service;
    exports org.example.salsiaopf.util;
    exports org.example.salsiaopf.ventas;
}
