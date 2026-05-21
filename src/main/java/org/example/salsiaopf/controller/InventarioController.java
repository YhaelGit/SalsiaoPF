package org.example.salsiaopf.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.salsiaopf.dao.InventarioDAO;
import org.example.salsiaopf.model.Ingrediente;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.Navegacion;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

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
    @FXML private TextField txtNombreIngrediente;
    @FXML private TextField txtCostoIngrediente;
    @FXML private TextField txtStockActual;
    @FXML private TableView<Ingrediente> tablaIngredientes;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        configurarTabla();
        mostrarIngredientes();
        cargarIngredientes();
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

    private void configurarTabla() {
        if (tablaIngredientes == null) return;
        configurarColumna(0, i -> String.valueOf(i.getId()));
        configurarColumna(1, Ingrediente::getNombre);
        configurarColumna(3, i -> String.format("%.2f", i.getCosto()));
        configurarColumna(4, i -> String.valueOf(i.getStock()));
    }

    @SuppressWarnings("unchecked")
    private void configurarColumna(int indice, Function<Ingrediente, String> extractor) {
        if (tablaIngredientes.getColumns().size() <= indice) return;
        TableColumn<Ingrediente, String> columna =
                (TableColumn<Ingrediente, String>) tablaIngredientes.getColumns().get(indice);
        columna.setCellValueFactory(cd -> {
            Ingrediente item = cd.getValue();
            String valor = item == null ? "" : extractor.apply(item);
            return new SimpleStringProperty(valor);
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

    @FXML
    private void mostrarNotificaciones() {
        Alertas.advertencia("Notificaciones", "Módulo de notificaciones en desarrollo.");
    }

    private void ocultarTodas() {
        VBox[] vistas = {viewIngredientes, viewExistencias, viewMovimientos, viewVencimientos, viewHistorialInventario};
        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML private void mostrarIngredientes() { ocultarTodas(); viewIngredientes.setVisible(true); viewIngredientes.setManaged(true); }
    @FXML private void mostrarExistencias() { ocultarTodas(); viewExistencias.setVisible(true); viewExistencias.setManaged(true); }
    @FXML private void mostrarMovimientos() { ocultarTodas(); viewMovimientos.setVisible(true); viewMovimientos.setManaged(true); }
    @FXML private void mostrarVencimientos() { ocultarTodas(); viewVencimientos.setVisible(true); viewVencimientos.setManaged(true); }
    @FXML private void mostrarHistorialInventario() { ocultarTodas(); viewHistorialInventario.setVisible(true); viewHistorialInventario.setManaged(true); }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void guardarIngrediente() {
        try {
            String nombre = txtNombreIngrediente != null ? txtNombreIngrediente.getText().trim() : "";
            if (nombre.isEmpty()) {
                Alertas.advertencia("Validación", "Ingrese el nombre del ingrediente.");
                return;
            }

            double costo = txtCostoIngrediente != null && !txtCostoIngrediente.getText().isEmpty()
                    ? Double.parseDouble(txtCostoIngrediente.getText()) : 0.0;
            int stock = txtStockActual != null && !txtStockActual.getText().isEmpty()
                    ? Integer.parseInt(txtStockActual.getText()) : 0;

            if (InventarioDAO.guardarIngrediente(nombre, costo, stock)) {
                Alertas.exito("Inventario", "Ingrediente guardado correctamente en SQL Server.");
                limpiarIngrediente();
                cargarIngredientes();
            } else {
                Alertas.error("Inventario", "No se pudo guardar. Verifique tbl_INVENTARIO.");
            }
        } catch (NumberFormatException e) {
            Alertas.advertencia("Validación", "Costo y stock deben ser valores numéricos válidos.");
        }
    }

    @FXML
    private void limpiarIngrediente() {
        if (txtNombreIngrediente != null) txtNombreIngrediente.clear();
        if (txtCostoIngrediente != null) txtCostoIngrediente.clear();
        if (txtStockActual != null) txtStockActual.clear();
    }

    private void cargarIngredientes() {
        if (tablaIngredientes != null) {
            tablaIngredientes.setItems(FXCollections.observableArrayList(InventarioDAO.listar()));
        }
    }
}
