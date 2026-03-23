package org.example.salsiaopf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 720);
        scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());

        stage.setTitle("Salsiao · Proceso de Compra");
        stage.setMinWidth(1060);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
