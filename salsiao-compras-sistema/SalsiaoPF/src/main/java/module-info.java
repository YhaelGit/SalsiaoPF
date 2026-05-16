module org.example.salsiaopf {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.salsiaopf to javafx.fxml;
    exports org.example.salsiaopf;
}