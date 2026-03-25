package org.example.salsiaopf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InventarioController {

    @FXML private ImageView logoImage;

    @FXML private VBox viewIngredientes;
    @FXML private VBox viewStock;
    @FXML private VBox viewReportes;

    @FXML
    private void initialize() {
        cargarLogo();
        showOnly(viewIngredientes);
    }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo");
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = {viewIngredientes, viewStock, viewReportes};

        for (VBox v : all) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showIngredientes() { showOnly(viewIngredientes); }
    @FXML private void showStock() { showOnly(viewStock); }
    @FXML private void showReportes() { showOnly(viewReportes); }

    @FXML
    private void volverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Sistema Principal");
            stage.show();

        } catch (Exception e) {
            System.out.println("Error volviendo al menú: " + e.getMessage());
        }
    }
}