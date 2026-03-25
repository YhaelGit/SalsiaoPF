package org.example.salsiaopf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class VentaController {

    @FXML private ImageView logoImage;

    @FXML private VBox viewVenta;
    @FXML private VBox viewDetalle;
    @FXML private VBox viewPago;
    @FXML private VBox viewHistorial;

    @FXML
    private void initialize() {
        cargarLogo();
        showOnly(viewVenta);
    }

    // Mostrar solo un segmento
    private void showOnly(VBox target) {
        VBox[] views = {viewVenta, viewDetalle, viewPago, viewHistorial};

        for (VBox v : views) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showVenta() { showOnly(viewVenta); }
    @FXML private void showDetalle() { showOnly(viewDetalle); }
    @FXML private void showPago() { showOnly(viewPago); }
    @FXML private void showHistorial() { showOnly(viewHistorial); }

    // Cargar logo redondo
    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));

                Circle clip = new Circle();
                clip.setRadius(35);
                clip.setCenterX(35);
                clip.setCenterY(35);

                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    // Volver al menú principal
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
            e.printStackTrace();
        }
    }
}