package org.example.salsiaopf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.net.URL;

public class EmpleadosController {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segEmpleado;
    @FXML private ToggleButton segDocumentos;
    @FXML private ToggleButton segContratacion;
    @FXML private ToggleButton segCapacitacion;
    @FXML private ToggleButton segConflictos;

    @FXML private VBox viewEmpleado;
    @FXML private VBox viewDocumentos;
    @FXML private VBox viewContratacion;
    @FXML private VBox viewCapacitacion;
    @FXML private VBox viewConflictos;

    @FXML
    private void initialize() {
        cargarLogo();

        if (segEmpleado != null) {
            segEmpleado.setSelected(true);
        }
        showOnly(viewEmpleado);
    }

    private void cargarLogo() {
        try {
            if (logoImage != null) {
                var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");

                if (stream == null) {
                    System.out.println("❌ No se encontró la imagen en /imagenes/logo-salsiao.jpeg");
                    return;
                }

                Image logo = new Image(stream);
                logoImage.setImage(logo);

                logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
            }
        } catch (Exception e) {
            System.out.println("❌ Error cargando imagen: " + e.getMessage());
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = {
                viewEmpleado,
                viewDocumentos,
                viewContratacion,
                viewCapacitacion,
                viewConflictos
        };

        for (VBox v : all) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showEmpleado() { showOnly(viewEmpleado); }
    @FXML private void showDocumentos() { showOnly(viewDocumentos); }
    @FXML private void showContratacion() { showOnly(viewContratacion); }
    @FXML private void showCapacitacion() { showOnly(viewCapacitacion); }
    @FXML private void showConflictos() { showOnly(viewConflictos); }

    @FXML
    private void volverMenu(ActionEvent event) {
        try {
            URL fxml = getClass().getResource("main.fxml");

            if (fxml == null) {
                throw new IllegalStateException("No se encontro main.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxml);
            Scene scene = new Scene(loader.load(), 1200, 800);

            URL css = getClass().getResource("styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

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