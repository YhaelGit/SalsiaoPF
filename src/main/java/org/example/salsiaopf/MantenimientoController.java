package org.example.salsiaopf;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class MantenimientoController {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segLocal;
    @FXML private ToggleButton segArea;
    @FXML private ToggleButton segEquipo;
    @FXML private ToggleButton segTecnico;
    @FXML private ToggleButton segConserje;
    @FXML private ToggleButton segLimpieza;
    @FXML private ToggleButton segMantenimiento;

    @FXML private VBox viewLocal;
    @FXML private VBox viewArea;
    @FXML private VBox viewEquipo;
    @FXML private VBox viewTecnico;
    @FXML private VBox viewConserje;
    @FXML private VBox viewLimpieza;
    @FXML private VBox viewMantenimiento;

    @FXML
    private void initialize() {
        cargarLogo();
        segLocal.setSelected(true);
        showOnly(viewLocal);
    }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");

            if (stream == null) {
                System.out.println("No se encontró la imagen");
                return;
            }

            Image logo = new Image(stream);
            logoImage.setImage(logo);

            // Hacer el logo redondo
            javafx.scene.shape.Circle clip =
                    new javafx.scene.shape.Circle(35, 35, 35);

            logoImage.setClip(clip);

        } catch (Exception e) {
            System.out.println("Error cargando imagen: " + e.getMessage());
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = {
                viewLocal,
                viewArea,
                viewEquipo,
                viewTecnico,
                viewConserje,
                viewLimpieza,
                viewMantenimiento
        };

        for (VBox v : all) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showLocal() { showOnly(viewLocal); }
    @FXML private void showArea() { showOnly(viewArea); }
    @FXML private void showEquipo() { showOnly(viewEquipo); }
    @FXML private void showTecnico() { showOnly(viewTecnico); }
    @FXML private void showConserje() { showOnly(viewConserje); }
    @FXML private void showLimpieza() { showOnly(viewLimpieza); }
    @FXML private void showMantenimiento() { showOnly(viewMantenimiento); }

    @FXML
    private void volverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("main.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                    getClass().getResource("styles.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Sistema Principal");
            stage.show();

        } catch (Exception e) {
            System.out.println("Error volviendo al menú: " + e.getMessage());
        }
    }
}