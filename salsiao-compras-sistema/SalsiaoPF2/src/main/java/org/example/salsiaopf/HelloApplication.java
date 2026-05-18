package org.example.salsiaopf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada principal de la aplicación Salsiao PF.
 * Arranca directamente en la pantalla de Login.
 * Una vez autenticado, el LoginController navega a main.fxml.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // ── Cargar la pantalla de Login ──────────────────────────────
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("login.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(
                HelloApplication.class.getResource("styles.css").toExternalForm()
        );

        stage.setTitle("Salsiao - Iniciar Sesión");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}