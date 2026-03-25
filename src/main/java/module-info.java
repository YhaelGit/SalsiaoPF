module org.example.salsiaopf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;


    opens org.example.salsiaopf to javafx.fxml;
    exports org.example.salsiaopf;
}