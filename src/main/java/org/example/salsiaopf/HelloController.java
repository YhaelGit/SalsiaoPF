package org.example.salsiaopf;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.example.salsiaopf.database.Conexion;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segOrder;
    @FXML private ToggleButton segSupplier;
    @FXML private ToggleButton segReception;
    @FXML private ToggleButton segIngredient;
    @FXML private ToggleButton segPayment;

    @FXML private VBox viewOrder;
    @FXML private VBox viewSupplier;
    @FXML private VBox viewReception;
    @FXML private VBox viewIngredient;
    @FXML private VBox viewPayment;

    // 🔥 conexión global
    private Connection conexion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cargarLogo();
        conectarBD();
        iniciarVista();
    }

    // ✅ MÉTODO PARA CARGAR LOGO (más limpio)
    private void cargarLogo() {
        try {
            if (logoImage != null) {
                Image logo = new Image(
                        getClass().getResourceAsStream("/org/example/imagenes/logo-salsiao.jpeg")
                );
                logoImage.setImage(logo);
            }
        } catch (Exception e) {
            System.out.println("❌ Error cargando imagen: " + e.getMessage());
        }
    }

    // ✅ MÉTODO PARA CONECTAR BD
    private void conectarBD() {
        try {
            conexion = Conexion.conectar();

            if (conexion != null) {
                System.out.println("🔥 Conectado a SQL Server correctamente");
            } else {
                System.out.println("❌ No se pudo conectar a la BD");
            }

        } catch (Exception e) {
            System.out.println("❌ Error en conexión: " + e.getMessage());
        }
    }

    // ✅ INICIAR VISTA
    private void iniciarVista() {
        if (segOrder != null) {
            segOrder.setSelected(true);
        }
        showOnly(viewOrder);
    }

    // 🔁 CONTROL DE VISTAS
    private void showOnly(VBox target) {
        VBox[] all = { viewOrder, viewSupplier, viewReception, viewIngredient, viewPayment };

        for (VBox v : all) {
            if (v != null) {
                boolean activo = (v == target);
                v.setVisible(activo);
                v.setManaged(activo);
            }
        }
    }

    // 🎛️ BOTONES DE NAVEGACIÓN
    @FXML private void showOrder() { showOnly(viewOrder); }
    @FXML private void showSupplier() { showOnly(viewSupplier); }
    @FXML private void showReception() { showOnly(viewReception); }
    @FXML private void showIngredient() { showOnly(viewIngredient); }
    @FXML private void showPayment() { showOnly(viewPayment); }

    // 🔥 BOTÓN PARA PROBAR CONEXIÓN
    @FXML
    private void probarConexion() {
        Connection con = Conexion.conectar();

        if (con != null) {
            System.out.println("✅ Conexión manual exitosa");
        } else {
            System.out.println("❌ Error en conexión manual");
        }
    }
}