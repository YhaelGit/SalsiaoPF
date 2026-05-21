package org.example.salsiaopf.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.salsiaopf.util.Navegacion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class VentaController {

    @FXML private VBox viewProductos;
    @FXML private VBox viewControlVenta;
    @FXML private VBox viewOrdenActual;
    @FXML private VBox viewPago;
    @FXML private VBox viewFacturacion;
    @FXML private VBox viewPedidosPendientes;
    @FXML private VBox viewDelivery;
    @FXML private VBox viewMesas;
    @FXML private VBox viewHistorialVentas;
    @FXML private VBox viewCaja;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private ImageView logoImage;
    @FXML private TextField txtMontoRecibido;
    @FXML private Label lblCambio;

    @FXML
    public void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarControlVenta();
        txtMontoRecibido.textProperty().addListener((obs, oldValue, newValue) -> calcularCambio());
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

    private void calcularCambio() {

        try {

            double total = 3363.00;

            double recibido = Double.parseDouble(
                    txtMontoRecibido.getText().isEmpty()
                            ? "0"
                            : txtMontoRecibido.getText()
            );

            double cambio = recibido - total;

            lblCambio.setText("RD$ " + String.format("%,.2f", cambio));

        } catch (Exception e) {

            lblCambio.setText("RD$ 0.00");

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

        viewProductos.setVisible(false);
        viewProductos.setManaged(false);

        viewControlVenta.setVisible(false);
        viewControlVenta.setManaged(false);

        viewOrdenActual.setVisible(false);
        viewOrdenActual.setManaged(false);

        viewPago.setVisible(false);
        viewPago.setManaged(false);

        viewFacturacion.setVisible(false);
        viewFacturacion.setManaged(false);

        viewPedidosPendientes.setVisible(false);
        viewPedidosPendientes.setManaged(false);

        viewDelivery.setVisible(false);
        viewDelivery.setManaged(false);

        viewMesas.setVisible(false);
        viewMesas.setManaged(false);

        viewHistorialVentas.setVisible(false);
        viewHistorialVentas.setManaged(false);

        viewCaja.setVisible(false);
        viewCaja.setManaged(false);
    }

    @FXML
    private void mostrarProductos() {
        ocultarTodas();
        viewProductos.setVisible(true);
        viewProductos.setManaged(true);
    }

    @FXML
    private void mostrarControlVenta() {
        ocultarTodas();
        viewControlVenta.setVisible(true);
        viewControlVenta.setManaged(true);
    }

    @FXML
    private void mostrarOrdenActual() {
        ocultarTodas();
        viewOrdenActual.setVisible(true);
        viewOrdenActual.setManaged(true);
    }

    @FXML
    private void mostrarPago() {
        ocultarTodas();
        viewPago.setVisible(true);
        viewPago.setManaged(true);
    }

    @FXML
    private void mostrarFacturacion() {
        ocultarTodas();
        viewFacturacion.setVisible(true);
        viewFacturacion.setManaged(true);
    }

    @FXML
    private void mostrarPedidosPendientes() {
        ocultarTodas();
        viewPedidosPendientes.setVisible(true);
        viewPedidosPendientes.setManaged(true);
    }

    @FXML
    private void mostrarDelivery() {
        ocultarTodas();
        viewDelivery.setVisible(true);
        viewDelivery.setManaged(true);
    }

    @FXML
    private void mostrarMesas() {
        ocultarTodas();
        viewMesas.setVisible(true);
        viewMesas.setManaged(true);
    }

    @FXML
    private void mostrarHistorialVentas() {
        ocultarTodas();
        viewHistorialVentas.setVisible(true);
        viewHistorialVentas.setManaged(true);
    }

    @FXML
    private void mostrarCaja() {
        ocultarTodas();
        viewCaja.setVisible(true);
        viewCaja.setManaged(true);
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