package org.example.salsiaopf.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.salsiaopf.util.Navegacion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InventarioController {

    @FXML private ImageView logoImage;

    @FXML private VBox viewIngredientes;
    @FXML private VBox viewExistencias;
    @FXML private VBox viewMovimientos;
    @FXML private VBox viewVencimientos;
    @FXML private VBox viewHistorialInventario;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;


    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarIngredientes();
    }

    private void cargarLogo() {
        try {

            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");

            if (stream != null && logoImage != null) {

                logoImage.setImage(new Image(stream));

                logoImage.setFitWidth(82);
                logoImage.setFitHeight(82);
                logoImage.setPreserveRatio(true);

                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle();

                clip.setRadius(41);

                clip.setCenterX(41);
                clip.setCenterY(41);

                logoImage.setClip(clip);
            }

        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void iniciarReloj() {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("hh:mm a");

        Timeline reloj = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> {
                    lblFechaActual.setText(LocalDate.now().format(formatoFecha));
                    lblHoraActual.setText(LocalTime.now().format(formatoHora));
                }),
                new KeyFrame(Duration.seconds(1))
        );

        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    @FXML
    private void mostrarNotificaciones() {
        System.out.println("Notificaciones pendientes de conexión a base de datos.");
    }

    private void ocultarTodas() {
        VBox[] vistas = {
                viewIngredientes,
                viewExistencias,
                viewMovimientos,
                viewVencimientos,
                viewHistorialInventario
        };

        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML
    private void mostrarIngredientes() {
        ocultarTodas();
        viewIngredientes.setVisible(true);
        viewIngredientes.setManaged(true);
    }

    @FXML
    private void mostrarExistencias() {
        ocultarTodas();
        viewExistencias.setVisible(true);
        viewExistencias.setManaged(true);
    }

    @FXML
    private void mostrarMovimientos() {
        ocultarTodas();
        viewMovimientos.setVisible(true);
        viewMovimientos.setManaged(true);
    }

    @FXML
    private void mostrarVencimientos() {
        ocultarTodas();
        viewVencimientos.setVisible(true);
        viewVencimientos.setManaged(true);
    }

    @FXML
    private void mostrarHistorialInventario() {
        ocultarTodas();
        viewHistorialInventario.setVisible(true);
        viewHistorialInventario.setManaged(true);
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }
}