package org.example.salsiaopf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del menú principal (main.fxml).
 * Muestra los módulos disponibles según el rol del usuario activo.
 * Incluye chip de sesión y botón de cerrar sesión.
 */
public class MainController implements Initializable {

    // ── Campos FXML ──────────────────────────────────────────────────────
    @FXML private ImageView logoImage;

    // Chip de sesión (nombre + rol)
    @FXML private Label lblUsuarioNombre;
    @FXML private Label lblUsuarioRol;

    // Botones de módulos (para ocultar según rol)
    @FXML private Button btnCompras;
    @FXML private Button btnEmpleados;
    @FXML private Button btnMantenimiento;
    @FXML private Button btnInventario;
    @FXML private Button btnClientes;
    @FXML private Button btnVentas;

    // ── Inicialización ────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarLogo();
        mostrarInfoSesion();
        aplicarPermisosPorRol();
    }

    /** Carga el logo circular de Salsiao. */
    private void cargarLogo() {
        try {
            if (logoImage != null) {
                var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
                if (stream == null) { System.out.println("No se encontró el logo"); return; }
                Image logo = new Image(stream);
                logoImage.setImage(logo);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(75, 75, 75);
                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    /**
     * Muestra el nombre y rol del usuario activo en el chip de sesión.
     * Si por algún motivo no hay sesión, cierra la aplicación.
     */
    private void mostrarInfoSesion() {
        SessionManager session = SessionManager.getInstance();

        if (!session.haySesionActiva()) {
            System.out.println("[MainController] Sin sesión activa — regresando al login.");
            return;
        }

        Usuario u = session.getUsuarioActivo();

        if (lblUsuarioNombre != null) {
            lblUsuarioNombre.setText(u.getIconoRol() + "  " + u.getNombre());
        }
        if (lblUsuarioRol != null) {
            lblUsuarioRol.setText(u.getRol());
        }
    }

    /**
     * Oculta los botones de módulos que el rol activo no puede ver.
     * Los botones siguen existiendo en el FXML pero no son visibles
     * ni ocupan espacio (managed=false).
     */
    private void aplicarPermisosPorRol() {
        // Módulo → nombre de botón FXML
        configurarBoton(btnCompras,        "compras");
        configurarBoton(btnEmpleados,      "empleados");
        configurarBoton(btnMantenimiento,  "mantenimiento");
        configurarBoton(btnInventario,     "inventario");
        configurarBoton(btnClientes,       "clientes");
        configurarBoton(btnVentas,         "ventas");
    }

    /** Oculta un botón si el rol activo no tiene permiso para ese módulo. */
    private void configurarBoton(Button btn, String modulo) {
        if (btn == null) return;
        boolean tienePermiso = RoleGuard.tienePermiso(modulo);
        btn.setVisible(tienePermiso);
        btn.setManaged(tienePermiso);
    }

    // ── Navegación a módulos (con RoleGuard) ─────────────────────────────

    @FXML
    private void abrirCompras(ActionEvent event) throws IOException {
        if (!RoleGuard.permitir("Compras")) return;
        cambiarEscena(event, "hello-view.fxml", "Salsiao - Compras");
    }

    @FXML
    private void abrirMantenimiento(ActionEvent event) throws IOException {
        if (!RoleGuard.permitir("Mantenimiento")) return;
        cambiarEscena(event, "mantenimiento.fxml", "Salsiao - Mantenimiento");
    }

    @FXML
    private void abrirEmpleados(ActionEvent event) throws IOException {
        if (!RoleGuard.permitir("Empleados")) return;
        cambiarEscena(event, "empleados.fxml", "Salsiao - Gestión de Empleados");
    }

    @FXML
    private void abrirInventario(ActionEvent event) throws IOException {
        if (!RoleGuard.permitir("Inventario")) return;
        cambiarEscena(event, "inventario.fxml", "Salsiao - Inventario");
    }

    @FXML
    private void abrirClientes(ActionEvent event) throws IOException {
        if (!RoleGuard.permitir("Clientes")) return;
        cambiarEscena(event, "cliente.fxml", "Salsiao - Gestión de Clientes");
    }

    @FXML
    private void abrirVentas(ActionEvent event) throws IOException {
        if (!RoleGuard.permitir("Ventas")) return;
        cambiarEscena(event, "venta.fxml", "Salsiao - Gestión de Ventas");
    }

    // ── Cerrar sesión ─────────────────────────────────────────────────────
    @FXML
    private void cerrarSesion(ActionEvent event) {
        // Confirmar antes de cerrar sesión
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar Sesión");
        confirm.setHeaderText("¿Deseas cerrar la sesión actual?");
        confirm.setContentText("Serás redirigido a la pantalla de inicio de sesión.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Limpiar la sesión
            SessionManager.getInstance().cerrarSesion();

            // Redirigir al Login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Scene scene = new Scene(loader.load(), 1200, 800);
                scene.getStylesheets().add(
                    getClass().getResource("styles.css").toExternalForm());

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Salsiao - Iniciar Sesión");
                stage.show();
            } catch (IOException e) {
                System.out.println("[MainController] Error al cerrar sesión: " + e.getMessage());
            }
        }
    }

    // ── Salir del sistema ─────────────────────────────────────────────────
    @FXML
    private void salirSistema(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // ── Helper de navegación ──────────────────────────────────────────────
    private void cambiarEscena(ActionEvent event, String fxml, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Scene scene = new Scene(loader.load(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(titulo);
        stage.show();
    }
}