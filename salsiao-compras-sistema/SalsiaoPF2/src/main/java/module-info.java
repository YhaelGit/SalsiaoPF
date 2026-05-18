module org.example.salsiaopf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires jbcrypt;

    opens org.example.salsiaopf to javafx.fxml;
    exports org.example.salsiaopf;
}