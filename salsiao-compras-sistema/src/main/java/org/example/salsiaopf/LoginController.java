package org.example.salsiaopf;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de Login.
 * Maneja validación, animaciones y autenticación contra la BD.
 */
public class LoginController implements Initializable {

    // ── Campos FXML ──────────────────────────────────────────────────────
    @FXML private VBox      panelLogin;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private TextField txtContrasenaVisible;   // campo visible al alternar
    @FXML private Button    btnMostrarContrasena;
    @FXML private Button    btnIngresar;
    @FXML private Label     lblError;
    @FXML private Label     lblVersion;
    @FXML private HBox      hboxContrasena;
    @FXML private ProgressIndicator progressLogin;

    // Estado del toggle de contraseña
    private boolean contrasenaVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ocultar elementos inicialmente
        lblError.setVisible(false);
        lblError.setManaged(false);
        txtContrasenaVisible.setVisible(false);
        txtContrasenaVisible.setManaged(false);
        if (progressLogin != null) {
            progressLogin.setVisible(false);
        }

        // Sincronizar campos de contraseña (visible ↔ oculto)
        txtContrasenaVisible.textProperty().addListener((obs, old, val) -> {
            if (contrasenaVisible) txtContrasena.setText(val);
        });
        txtContrasena.textProperty().addListener((obs, old, val) -> {
            if (!contrasenaVisible) txtContrasenaVisible.setText(val);
        });

        // Animación de entrada del panel
        animarEntrada();

        // Enfocar el campo de usuario al inicio
        Platform.runLater(() -> txtUsuario.requestFocus());
    }

    // ── Login principal ───────────────────────────────────────────────────
    @FXML
    private void iniciarSesion() {
        String usuarioTexto = txtUsuario.getText().trim();
        String contrasena = contrasenaVisible
                ? txtContrasenaVisible.getText()
                : txtContrasena.getText();

        // Validación de campos vacíos
        if (usuarioTexto.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor completa todos los campos.");
            sacudirPanel();
            return;
        }

        // Mostrar indicador de carga
        btnIngresar.setDisable(true);
        mostrarCargando(true);
        ocultarError();

        // Autenticar en hilo separado (no bloquear el hilo de JavaFX)
        Thread hiloAuth = new Thread(() -> {
            Usuario usuario = UsuarioDAO.autenticar(usuarioTexto, contrasena);

            Platform.runLater(() -> {
                mostrarCargando(false);
                btnIngresar.setDisable(false);

                if (usuario != null) {
                    // ✅ Credenciales correctas → iniciar sesión
                    SessionManager.getInstance().iniciarSesion(usuario);
                    abrirMenuPrincipal();
                } else {
                    // ❌ Credenciales incorrectas
                    mostrarError("Usuario o contraseña incorrectos.");
                    sacudirPanel();
                    txtContrasena.clear();
                    txtContrasenaVisible.clear();
                }
            });
        });
        hiloAuth.setDaemon(true);
        hiloAuth.start();
    }

    // ── Toggle mostrar/ocultar contraseña ─────────────────────────────────
    @FXML
    private void toggleContrasena() {
        contrasenaVisible = !contrasenaVisible;

        if (contrasenaVisible) {
            // Copiar texto y mostrar campo visible
            txtContrasenaVisible.setText(txtContrasena.getText());
            txtContrasena.setVisible(false);
            txtContrasena.setManaged(false);
            txtContrasenaVisible.setVisible(true);
            txtContrasenaVisible.setManaged(true);
            btnMostrarContrasena.setText("🙈");
            Platform.runLater(() -> txtContrasenaVisible.requestFocus());
        } else {
            // Copiar texto y mostrar campo oculto
            txtContrasena.setText(txtContrasenaVisible.getText());
            txtContrasenaVisible.setVisible(false);
            txtContrasenaVisible.setManaged(false);
            txtContrasena.setVisible(true);
            txtContrasena.setManaged(true);
            btnMostrarContrasena.setText("👁");
            Platform.runLater(() -> txtContrasena.requestFocus());
        }
    }

    // ── Navegar al menú principal ─────────────────────────────────────────
    private void abrirMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());

            Stage stage = (Stage) btnIngresar.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Sistema Principal");
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al cargar el menú: " + e.getMessage());
            System.out.println("[LoginController] " + e.getMessage());
        }
    }

    // ── Helpers UI ────────────────────────────────────────────────────────
    private void mostrarError(String mensaje) {
        lblError.setText("⚠  " + mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);

        // Animación fade-in del error
        FadeTransition ft = new FadeTransition(Duration.millis(250), lblError);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void mostrarCargando(boolean mostrar) {
        if (progressLogin != null) {
            progressLogin.setVisible(mostrar);
        }
        btnIngresar.setText(mostrar ? "Verificando..." : "Ingresar");
    }

    /** Animación de sacudida horizontal para indicar error. */
    private void sacudirPanel() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), panelLogin);
        tt.setFromX(0); tt.setToX(10); tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    /** Animación de entrada con fade + slide al abrir la pantalla. */
    private void animarEntrada() {
        panelLogin.setOpacity(0);
        panelLogin.setTranslateY(20);

        FadeTransition ft = new FadeTransition(Duration.millis(600), panelLogin);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(600), panelLogin);
        tt.setFromY(20);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.setDelay(Duration.millis(100));
        pt.play();
    }
}
