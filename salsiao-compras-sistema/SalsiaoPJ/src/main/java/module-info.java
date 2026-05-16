module org.example.salsiaopj {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.salsiaopj to javafx.fxml;
    exports org.example.salsiaopj;
}