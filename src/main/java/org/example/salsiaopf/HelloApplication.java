package org.example.salsiaopf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/org/example/salsiaopf/centrosistema.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(
                HelloApplication.class.getResource("styles.css").toExternalForm()
        );

        stage.setTitle("Salsiao");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}