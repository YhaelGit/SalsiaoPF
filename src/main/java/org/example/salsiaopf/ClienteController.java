package org.example.salsiaopf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClienteController {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segCliente;
    @FXML private ToggleButton segHistorial;

    @FXML private VBox viewCliente;
    @FXML private VBox viewHistorial;

    @FXML private ToggleButton segRecepcion;
    @FXML private VBox viewRecepcion;


    @FXML
    private void initialize() {
        cargarLogo();

        if (segCliente != null) {
            segCliente.setSelected(true);
        }

        showOnly(viewCliente);
    }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = { viewCliente, viewHistorial, viewRecepcion };

        for (VBox v : all) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }


    @FXML
    private void showCliente() {
        showOnly(viewCliente);
    }

    @FXML
    private void showHistorial() {
        showOnly(viewHistorial);
    }

    @FXML
    private void showRecepcion() {
        showOnly(viewRecepcion);
    }

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