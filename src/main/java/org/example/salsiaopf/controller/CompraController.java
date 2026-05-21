package org.example.salsiaopf.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.salsiaopf.dao.CompraDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.Navegacion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CompraController {

    @FXML private ImageView logoImage;
    @FXML private VBox viewOrdenCompra;
    @FXML private VBox viewProveedores;
    @FXML private VBox viewRecepcion;
    @FXML private VBox viewAnalisisCompras;
    @FXML private VBox viewPagosCompra;
    @FXML private VBox viewHistorialCompras;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private TextField txtProveedorOrden;
    @FXML private TextField txtTotalOrden;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarOrdenCompra();
        animarEntrada();
    }

    private void animarEntrada() {
        Platform.runLater(() -> {
            if (lblFechaActual == null || lblFechaActual.getScene() == null) return;
            Node root = lblFechaActual.getScene().getRoot();
            if (root == null) return;
            root.setOpacity(0);
            root.setTranslateY(20);
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0); ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), root);
            tt.setFromY(20); tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition pt = new ParallelTransition(ft, tt);
            pt.setDelay(Duration.millis(80));
            pt.play();
        });
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

    private void ocultarTodas() {
        VBox[] vistas = {viewOrdenCompra, viewProveedores, viewRecepcion, viewAnalisisCompras, viewPagosCompra, viewHistorialCompras};
        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML private void mostrarOrdenCompra() { ocultarTodas(); mostrarVista(viewOrdenCompra); }
    @FXML private void mostrarProveedores() { ocultarTodas(); mostrarVista(viewProveedores); }
    @FXML private void mostrarRecepcion() { ocultarTodas(); mostrarVista(viewRecepcion); }
    @FXML private void mostrarAnalisisCompras() { ocultarTodas(); mostrarVista(viewAnalisisCompras); }
    @FXML private void mostrarPagosCompra() { ocultarTodas(); mostrarVista(viewPagosCompra); }
    @FXML private void mostrarHistorialCompras() { ocultarTodas(); mostrarVista(viewHistorialCompras); }

    private void mostrarVista(VBox vista) {
        if (vista != null) {
            vista.setVisible(true);
            vista.setManaged(true);
        }
    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.advertencia("Notificaciones", "Módulo de notificaciones en desarrollo.");
    }

    @FXML
    private void guardarOrdenCompra() {
        String proveedor = txtProveedorOrden != null ? txtProveedorOrden.getText().trim() : "";
        if (proveedor.isEmpty()) {
            Alertas.advertencia("Validación", "Ingrese el nombre del proveedor.");
            return;
        }

        try {
            double total = txtTotalOrden != null && !txtTotalOrden.getText().isEmpty()
                    ? Double.parseDouble(txtTotalOrden.getText()) : 0.0;

            if (CompraDAO.guardarCompra(proveedor, total)) {
                Alertas.exito("Compras", "Orden de compra guardada en SQL Server.");
                limpiarOrdenCompra();
            } else {
                Alertas.error("Compras", "No se pudo guardar. Verifique tbl_COMPRA.");
            }
        } catch (NumberFormatException e) {
            Alertas.advertencia("Validación", "El total debe ser un número válido.");
        }
    }

    @FXML
    private void limpiarOrdenCompra() {
        if (txtProveedorOrden != null) txtProveedorOrden.clear();
        if (txtTotalOrden != null) txtTotalOrden.clear();
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
