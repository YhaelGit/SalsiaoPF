module org.example.salsiaopf {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires jbcrypt;

    opens org.example.salsiaopf to javafx.fxml;
    opens org.example.salsiaopf.controller to javafx.fxml;

    exports org.example.salsiaopf;
    exports org.example.salsiaopf.controller;
}