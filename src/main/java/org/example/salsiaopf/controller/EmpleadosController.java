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
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EmpleadosController {

    @FXML private ImageView logoImage;

    @FXML private VBox viewRegistroEmpleado;
    @FXML private VBox viewNomina;
    @FXML private VBox viewUsuariosSistema;
    @FXML private VBox viewRolesPermisos;
    @FXML private VBox viewAsistencia;
    @FXML private VBox viewHorarios;
    @FXML private VBox viewHistorialEmpleados;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;

    @FXML private TextField txtNombreEmpleado;
    @FXML private TextField txtApellidoEmpleado;
    @FXML private TextField txtCedulaEmpleado;
    @FXML private TextField txtTelefonoEmpleado;
    @FXML private TextField txtCorreoEmpleado;
    @FXML private TextField txtDireccionEmpleado;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarRegistroEmpleado();
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
                viewRegistroEmpleado,
                viewNomina,
                viewUsuariosSistema,
                viewRolesPermisos,
                viewAsistencia,
                viewHorarios,
                viewHistorialEmpleados
        };

        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML
    private void mostrarRegistroEmpleado() {
        ocultarTodas();
        viewRegistroEmpleado.setVisible(true);
        viewRegistroEmpleado.setManaged(true);
    }

    @FXML
    private void mostrarNomina() {
        ocultarTodas();
        viewNomina.setVisible(true);
        viewNomina.setManaged(true);
    }

    @FXML
    private void mostrarUsuariosSistema() {
        ocultarTodas();
        viewUsuariosSistema.setVisible(true);
        viewUsuariosSistema.setManaged(true);
    }

    @FXML
    private void mostrarRolesPermisos() {
        ocultarTodas();
        viewRolesPermisos.setVisible(true);
        viewRolesPermisos.setManaged(true);
    }

    @FXML
    private void mostrarAsistencia() {
        ocultarTodas();
        viewAsistencia.setVisible(true);
        viewAsistencia.setManaged(true);
    }

    @FXML
    private void mostrarHorarios() {
        ocultarTodas();
        viewHorarios.setVisible(true);
        viewHorarios.setManaged(true);
    }

    @FXML
    private void mostrarHistorialEmpleados() {
        ocultarTodas();
        viewHistorialEmpleados.setVisible(true);
        viewHistorialEmpleados.setManaged(true);
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void guardarRegistroEmpleado() {
        boolean exito = org.example.salsiaopf.dao.EmpleadoDAO.guardarEmpleado(
                txtNombreEmpleado != null ? txtNombreEmpleado.getText() : "",
                txtApellidoEmpleado != null ? txtApellidoEmpleado.getText() : "",
                txtCedulaEmpleado != null ? txtCedulaEmpleado.getText() : "",
                txtTelefonoEmpleado != null ? txtTelefonoEmpleado.getText() : "",
                txtCorreoEmpleado != null ? txtCorreoEmpleado.getText() : "",
                txtDireccionEmpleado != null ? txtDireccionEmpleado.getText() : ""
        );

        if (exito) {
            System.out.println("Empleado guardado exitosamente.");
            if (txtNombreEmpleado != null) txtNombreEmpleado.clear();
            if (txtApellidoEmpleado != null) txtApellidoEmpleado.clear();
            if (txtCedulaEmpleado != null) txtCedulaEmpleado.clear();
            if (txtTelefonoEmpleado != null) txtTelefonoEmpleado.clear();
            if (txtCorreoEmpleado != null) txtCorreoEmpleado.clear();
            if (txtDireccionEmpleado != null) txtDireccionEmpleado.clear();
        } else {
            System.out.println("Error al guardar empleado.");
        }
    }
}