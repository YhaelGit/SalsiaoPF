package org.example.salsiaopf;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import org.example.salsiaopf.ConexionBD;
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

    private Connection conexion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cargarLogo();
        conectarBD();
        iniciarVista();
    }

    private void cargarLogo() {
        try {
            if (logoImage != null) {
                var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");

                if (stream == null) {
                    System.out.println("❌ No se encontró la imagen");
                    return;
                }

                Image logo = new Image(stream);
                logoImage.setImage(logo);

                Circle clip = new Circle(39, 39, 39);
                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("❌ Error cargando imagen: " + e.getMessage());
        }
    }

    private void conectarBD() {
        try {
            conexion = ConexionBD.conectar();
            if (conexion != null) {
                System.out.println("Conectado a SQL Server correctamente");
            } else {
                System.out.println("❌ No se pudo conectar a la BD");
            }

        } catch (Exception e) {
            System.out.println("❌ Error en conexión: " + e.getMessage());
        }
    }

    private void iniciarVista() {
        if (segOrder != null) {
            segOrder.setSelected(true);
        }
        showOnly(viewOrder);
    }

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

    @FXML
    private void volverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("main.fxml"));

            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                    getClass().getResource("styles.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Sistema Principal");
            stage.show();

        } catch (Exception e) {
            System.out.println("Error volviendo al menú: " + e.getMessage());
        }
    }

    @FXML private void showOrder() { showOnly(viewOrder); }
    @FXML private void showSupplier() { showOnly(viewSupplier); }
    @FXML private void showReception() { showOnly(viewReception); }
    @FXML private void showIngredient() { showOnly(viewIngredient); }
    @FXML private void showPayment() { showOnly(viewPayment); }

    @FXML
    private void probarConexion() {
        Connection con = ConexionBD.conectar();
        if (con != null) {
            System.out.println("Conexión manual exitosa");
        } else {
            System.out.println("❌ Error en conexión manual");
        }
    }
}

