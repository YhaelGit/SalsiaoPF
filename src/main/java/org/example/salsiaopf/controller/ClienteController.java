package org.example.salsiaopf.controller;

import org.example.salsiaopf.dao.ClienteDAO;
import org.example.salsiaopf.model.Cliente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.salsiaopf.database.ConexionBD;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClienteController {

    @FXML
    private ImageView logoImage;

    @FXML
    private ToggleButton segCliente;
    @FXML
    private ToggleButton segHistorial;
    @FXML
    private ToggleButton segRecepcion;

    @FXML
    private VBox viewCliente;
    @FXML
    private VBox viewHistorial;
    @FXML
    private VBox viewRecepcion;

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellido;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtDireccion;

    @FXML
    private VBox viewRegistro;

    @FXML
    private VBox viewReservas;

    @FXML
    private VBox viewReclamaciones;

    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarSolo(viewRegistro);

        if (segCliente != null) {
            segCliente.setSelected(true);
        }

        showOnly(viewCliente);

    }

    private void mostrarSolo(VBox vistaActiva) {

        VBox[] vistas = {
                viewRegistro,
                viewReservas,
                viewReclamaciones,
                viewHistorial
        };

        for (VBox vista : vistas) {

            boolean activa = (vista == vistaActiva);

            vista.setVisible(activa);
            vista.setManaged(activa);
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
        System.out.println("Notificaciones pendientes.");
    }

    @FXML
    private void mostrarRegistro() {
        mostrarSolo(viewRegistro);
    }

    @FXML
    private void mostrarReservas() {
        mostrarSolo(viewReservas);
    }

    @FXML
    private void mostrarReclamaciones() {
        mostrarSolo(viewReclamaciones);
    }

    @FXML
    private void mostrarHistorial() {
        mostrarSolo(viewHistorial);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
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

    private void showOnly(VBox target) {
        VBox[] all = { viewCliente, viewHistorial, viewRecepcion };

        for (VBox v : all) {
            if (v != null) {
                boolean active = (v == target);
                v.setVisible(active);
                v.setManaged(active);
            }
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
    private void guardarCliente() {
        String sql = """
                INSERT INTO tbl_CLIENTE
                (nombre, apellido, telefono)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = ConexionBD.conectar();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtApellido.getText());
            ps.setString(3, txtTelefono.getText());

            ps.executeUpdate();

            guardarDireccionCliente();

            txtNombre.clear();
            txtApellido.clear();
            txtTelefono.clear();
            txtDireccion.clear();

            System.out.println("Cliente guardado correctamente");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void guardarDireccionCliente() {
        String sql = """
                INSERT INTO tbl_DIRECCION
                (nombre, fk_ID_cliente)
                VALUES (?, IDENT_CURRENT('tbl_CLIENTE'))
                """;

        try (Connection conn = ConexionBD.conectar();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtDireccion.getText());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        try {
            URL fxml = getClass().getResource("/org/example/salsiaopf/centrosistema.fxml");

            if (fxml == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo centrosistema.fxml en /org/example/salsiaopf/centrosistema.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxml);
            Scene scene = new Scene(loader.load(), 1200, 800);

            URL css = getClass().getResource("/org/example/salsiaopf/styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Centro del Sistema");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}