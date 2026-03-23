module org.example.salsiaopf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.salsiaopf to javafx.fxml;
    exports org.example.salsiaopf;
}