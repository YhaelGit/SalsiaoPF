package org.example.salsiaopf.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navegacion {

    private static final String RUTA_BASE = "/org/example/salsiaopf/";
    private static final String CSS = "/org/example/salsiaopf/styles.css";

    public static void cambiarEscena(ActionEvent event, String archivoFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Navegacion.class.getResource(RUTA_BASE + archivoFxml)
            );

            Scene scene = new Scene(loader.load());

            var css = Navegacion.class.getResource(CSS);
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle(titulo);

            stage.setMaximized(false);
            stage.setMaximized(true);

            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);
                stage.show();
            });

        } catch (Exception e) {
            System.out.println("Error navegando a " + archivoFxml + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void volverCentroSistema(ActionEvent event) {
        cambiarEscena(event, "centrosistema.fxml", "Salsiao - Centro del Sistema");
    }

    public static void abrirClientes(ActionEvent event) {
        cambiarEscena(event, "cliente.fxml", "Salsiao - Clientes");
    }

    public static void abrirVentas(ActionEvent event) {
        cambiarEscena(event, "venta.fxml", "Salsiao - Ventas");
    }

    public static void abrirCompras(ActionEvent event) {
        cambiarEscena(event, "compra.fxml", "Salsiao - Compras");
    }

    public static void abrirInventario(ActionEvent event) {
        cambiarEscena(event, "inventario.fxml", "Salsiao - Inventario");
    }

    public static void abrirEmpleados(ActionEvent event) {
        cambiarEscena(event, "empleados.fxml", "Salsiao - Empleados");
    }

    public static void abrirMantenimiento(ActionEvent event) {
        cambiarEscena(event, "mantenimiento.fxml", "Salsiao - Mantenimiento");
    }
}