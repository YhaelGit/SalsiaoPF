package org.example.salsiaopf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ImageView logoImage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarLogo();
    }

    private void cargarLogo() {
        try {
            if (logoImage != null) {
                var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");

                if (stream == null) {
                    System.out.println("No se encontró el logo");
                    return;
                }

                Image logo = new Image(stream);
                logoImage.setImage(logo);


                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(75, 75, 75);
                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    @FXML
    private void abrirCompras(ActionEvent event) throws IOException {
        cambiarEscena(event, "hello-view.fxml", "Salsiao - Compras");
    }

    @FXML
    private void abrirMantenimiento(ActionEvent event) throws IOException {
        cambiarEscena(event, "mantenimiento.fxml", "Salsiao - Mantenimiento");
    }

    @FXML
    private void abrirEmpleados(ActionEvent event) throws IOException {
        cambiarEscena(event, "empleados.fxml", "Salsiao - Gestión de Empleados");
    }

    @FXML
    private void abrirInventario(ActionEvent event) throws IOException {
        cambiarEscena(event, "inventario.fxml", "Salsiao - Inventario");
    }

    @FXML
    private void abrirClientes(ActionEvent event) throws IOException {
        cambiarEscena(event, "cliente.fxml", "Salsiao - Gestión de Clientes");
    }

    @FXML
    private void abrirVentas(ActionEvent event) throws IOException {
        cambiarEscena(event, "venta.fxml", "Salsiao - Gestión de Ventas");
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

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