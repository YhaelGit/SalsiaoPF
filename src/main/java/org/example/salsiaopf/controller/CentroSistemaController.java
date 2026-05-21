package org.example.salsiaopf.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.salsiaopf.util.Navegacion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.RoleGuard;

public class CentroSistemaController {

    @FXML
    private ImageView logoImage;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        actualizarNotificaciones();
    }

    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;

    private int cantidadNotificaciones = 3;

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

    private void cambiarEscena(String archivoFXML, String titulo, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/salsiaopf/" + archivoFXML)
            );

            Scene scene = new Scene(loader.load(), 1200, 800);

            scene.getStylesheets().add(
                    getClass().getResource("/org/example/salsiaopf/styles.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();

        } catch (Exception e) {
            System.out.println("Error abriendo " + archivoFXML + ": " + e.getMessage());
            e.printStackTrace();
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

    private void actualizarNotificaciones() {
        btnNotificaciones.setText("🔔 " + cantidadNotificaciones);
    }

    @FXML
    private void mostrarNotificaciones() {
        System.out.println("Mostrar panel de notificaciones");
    }

    @FXML
    private void mostrarGeneral(ActionEvent event) {
        System.out.println("Pantalla general");
    }

    @FXML
    private void irNuevaVenta(ActionEvent event) {
        if (!RoleGuard.permitir("ventas")) return;
        Navegacion.abrirVentas(event);
    }

    @FXML
    private void irNuevoCliente(ActionEvent event) {
        if (!RoleGuard.permitir("clientes")) return;
        Navegacion.abrirClientes(event);
    }

    @FXML
    private void irNuevaCompra(ActionEvent event) {
        if (!RoleGuard.permitir("compras")) return;
        Navegacion.abrirCompras(event);
    }

    @FXML
    private void irNuevoProducto(ActionEvent event) {
        if (!RoleGuard.permitir("inventario")) return;
        Navegacion.abrirInventario(event);
    }

    @FXML
    private void irNuevaReserva(ActionEvent event) {
        if (!RoleGuard.permitir("clientes")) return;
        Navegacion.abrirClientes(event);
    }

    @FXML
    private void irReporteVentas(ActionEvent event) {
        if (!RoleGuard.permitir("reportes")) return;
        System.out.println("Ir a reportes de ventas");
    }

    @FXML
    private void abrirVentas(ActionEvent event) {
        if (!RoleGuard.permitir("ventas")) return;
        Navegacion.abrirVentas(event);
    }

    @FXML
    private void abrirCompras(ActionEvent event) {
        if (!RoleGuard.permitir("compras")) return;
        Navegacion.abrirCompras(event);
    }

    @FXML
    private void abrirClientes(ActionEvent event) {
        if (!RoleGuard.permitir("clientes")) return;
        Navegacion.abrirClientes(event);
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        if (!RoleGuard.permitir("inventario")) return;
        Navegacion.abrirInventario(event);
    }

    @FXML
    private void abrirEmpleados(ActionEvent event) {
        if (!RoleGuard.permitir("empleados")) return;
        Navegacion.abrirEmpleados(event);
    }

    @FXML
    private void abrirMantenimiento(ActionEvent event) {
        if (!RoleGuard.permitir("mantenimiento")) return;
        Navegacion.abrirMantenimiento(event);
    }

    @FXML
    private void abrirReportes(ActionEvent event) {
        if (!RoleGuard.permitir("reportes")) return;
        System.out.println("Abrir reportes");
    }

    @FXML
    private void abrirConfiguracion(ActionEvent event) {
        System.out.println("Abrir configuración");
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }
}
