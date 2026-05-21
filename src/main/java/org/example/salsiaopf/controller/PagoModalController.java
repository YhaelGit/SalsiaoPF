package org.example.salsiaopf.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.ventas.CarritoVentas;
import org.example.salsiaopf.ventas.DatosProcesoPago;
import org.example.salsiaopf.ventas.ItemCarrito;
import org.example.salsiaopf.ventas.MetodoPago;
import org.example.salsiaopf.ventas.ResultadoProcesoVenta;
import org.example.salsiaopf.service.PagoService;

import java.util.function.Consumer;

/**
 * Modal de procesamiento de pago (POS).
 */
public class PagoModalController {

    @FXML private VBox panelResumenCompra;
    @FXML private Label lblTotalModal;
    @FXML private TextField txtEmailCliente;
    @FXML private ToggleButton btnEfectivo;
    @FXML private ToggleButton btnTarjeta;
    @FXML private ToggleButton btnTransferencia;
    @FXML private VBox panelEfectivo;
    @FXML private TextField txtMontoRecibidoModal;
    @FXML private Label lblDevueltaModal;
    @FXML private Button confirmarBtn;

    private ToggleGroup grupoPago;
    private CarritoVentas carrito;
    private double total;
    private Consumer<ResultadoProcesoVenta> onExito;
    private boolean confirmado;

    @FXML
    private void initialize() {
        grupoPago = new ToggleGroup();
        btnEfectivo.setToggleGroup(grupoPago);
        btnTarjeta.setToggleGroup(grupoPago);
        btnTransferencia.setToggleGroup(grupoPago);

        grupoPago.selectedToggleProperty().addListener((obs, o, n) -> actualizarPanelEfectivo());

        if (txtMontoRecibidoModal != null) {
            txtMontoRecibidoModal.textProperty().addListener((obs, o, n) -> calcularDevuelta());
        }

        actualizarPanelEfectivo();
    }

    public void configurar(CarritoVentas carritoRef, Consumer<ResultadoProcesoVenta> callbackExito) {
        this.carrito = carritoRef;
        this.onExito = callbackExito;
        this.total = carrito.getSubtotal();
        this.confirmado = false;

        if (lblTotalModal != null) {
            lblTotalModal.setText(String.format("RD$ %,.2f", total));
        }

        if (panelResumenCompra != null) {
            panelResumenCompra.getChildren().clear();
            for (ItemCarrito item : carrito.getItems()) {
                Label linea = new Label(String.format("%d× %s — RD$ %,.2f",
                        item.getCantidad(),
                        item.getProducto().getNombre(),
                        item.getSubtotal()));
                linea.getStyleClass().add("pagoResumenLinea");
                linea.setWrapText(true);
                panelResumenCompra.getChildren().add(linea);
            }
        }

        if (txtMontoRecibidoModal != null) {
            txtMontoRecibidoModal.setText(String.format("%.0f", total));
        }
        calcularDevuelta();
    }

    private void actualizarPanelEfectivo() {
        boolean esEfectivo = btnEfectivo.isSelected();
        if (panelEfectivo != null) {
            panelEfectivo.setVisible(esEfectivo);
            panelEfectivo.setManaged(esEfectivo);
        }
        if (esEfectivo) {
            calcularDevuelta();
        }
    }

    private void calcularDevuelta() {
        if (lblDevueltaModal == null || !btnEfectivo.isSelected()) return;
        try {
            double recibido = txtMontoRecibidoModal.getText().isBlank()
                    ? 0
                    : Double.parseDouble(txtMontoRecibidoModal.getText().trim());
            double devuelta = Math.max(0, recibido - total);
            lblDevueltaModal.setText(String.format("RD$ %,.2f", devuelta));
        } catch (NumberFormatException e) {
            lblDevueltaModal.setText("RD$ 0.00");
        }
    }

    private MetodoPago metodoSeleccionado() {
        if (btnTarjeta.isSelected()) return MetodoPago.TARJETA;
        if (btnTransferencia.isSelected()) return MetodoPago.TRANSFERENCIA;
        return MetodoPago.EFECTIVO;
    }

    @FXML
    private void confirmarPago() {
        String email = txtEmailCliente != null ? txtEmailCliente.getText().trim() : "";
        if (email.isEmpty() || !email.contains("@")) {
            Alertas.advertencia("Correo requerido", "Ingrese un correo válido para enviar la factura.");
            return;
        }

        MetodoPago metodo = metodoSeleccionado();
        double montoRecibido = 0;
        double devuelta = 0;

        if (metodo == MetodoPago.EFECTIVO) {
            try {
                montoRecibido = Double.parseDouble(txtMontoRecibidoModal.getText().trim());
            } catch (NumberFormatException e) {
                Alertas.advertencia("Monto inválido", "Ingrese el monto recibido en efectivo.");
                return;
            }
            if (montoRecibido < total) {
                Alertas.advertencia("Monto insuficiente",
                        String.format("El monto recibido debe ser al menos RD$ %,.2f", total));
                return;
            }
            devuelta = montoRecibido - total;
        }

        DatosProcesoPago datos = new DatosProcesoPago(email, metodo, montoRecibido, devuelta, total);

        if (confirmarBtn != null) {
            confirmarBtn.setDisable(true);
            confirmarBtn.setText("Procesando...");
        }

        Task<ResultadoProcesoVenta> task = new Task<>() {
            @Override
            protected ResultadoProcesoVenta call() {
                return PagoService.procesarPago(carrito, datos);
            }
        };

        task.setOnSucceeded(ev -> Platform.runLater(() -> {
            ResultadoProcesoVenta resultado = task.getValue();
            if (confirmarBtn != null) {
                confirmarBtn.setDisable(false);
                confirmarBtn.setText("✔ Confirmar pago");
            }
            if (resultado == null || !resultado.isExito()) {
                Alertas.error("Error en venta",
                        resultado != null ? resultado.getMensajeError() : "Error desconocido.");
                return;
            }
            confirmado = true;
            if (onExito != null) {
                onExito.accept(resultado);
            }
            cerrarModal();
        }));

        task.setOnFailed(ev -> Platform.runLater(() -> {
            if (confirmarBtn != null) {
                confirmarBtn.setDisable(false);
                confirmarBtn.setText("✔ Confirmar pago");
            }
            Alertas.error("Error", "No se pudo completar la venta.");
        }));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void cancelar() {
        confirmado = false;
        cerrarModal();
    }

    private void cerrarModal() {
        Stage stage = (Stage) panelResumenCompra.getScene().getWindow();
        stage.close();
    }

    public boolean fueConfirmado() {
        return confirmado;
    }
}
