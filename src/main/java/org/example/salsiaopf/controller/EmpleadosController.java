package org.example.salsiaopf.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.salsiaopf.dao.EmpleadoDAO;
import org.example.salsiaopf.model.Empleado;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class EmpleadosController {

    @FXML private ImageView logoImage;
    @FXML private VBox viewRegistroEmpleado;
    @FXML private VBox viewNomina;
    @FXML private VBox viewUsuariosSistema;
    @FXML private VBox viewRolesPermisos;
    @FXML private VBox viewAsistencia;
    @FXML private VBox viewHorarios;
    @FXML private VBox viewHistorialEmpleados;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private TextField txtNombreEmpleado;
    @FXML private TextField txtApellidoEmpleado;
    @FXML private TextField txtCedulaEmpleado;
    @FXML private TextField txtTelefonoEmpleado;
    @FXML private TextField txtCorreoEmpleado;
    @FXML private TextField txtDireccionEmpleado;
    @FXML private TableView<Empleado> tablaEmpleados;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        configurarTabla();
        mostrarRegistroEmpleado();
        cargarEmpleados();
        animarEntrada();
    }

    private void animarEntrada() {
        ControllerUtil.animarEntrada(lblFechaActual);
    }

    private void configurarTabla() {
        if (tablaEmpleados == null) return;
        configurarColumna(0, e -> String.valueOf(e.getId()));
        configurarColumna(1, e -> (e.getNombre() + " " + e.getApellido()).trim());
        configurarColumna(2, Empleado::getCedula);
        configurarColumna(3, Empleado::getTelefono);
    }

    @SuppressWarnings("unchecked")
    private void configurarColumna(int indice, Function<Empleado, String> extractor) {
        if (tablaEmpleados.getColumns().size() <= indice) return;
        TableColumn<Empleado, String> columna =
                (TableColumn<Empleado, String>) tablaEmpleados.getColumns().get(indice);
        columna.setCellValueFactory(cd -> {
            Empleado empleado = cd.getValue();
            String valor = empleado == null ? "" : extractor.apply(empleado);
            return new SimpleStringProperty(valor);
        });
    }

    private void cargarLogo() {
        ControllerUtil.cargarLogo(logoImage);
    }

    private void iniciarReloj() {
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.advertencia("Notificaciones", "Módulo de notificaciones en desarrollo.");
    }

    private void ocultarTodas() {
        VBox[] vistas = {
                viewRegistroEmpleado, viewNomina, viewUsuariosSistema,
                viewRolesPermisos, viewAsistencia, viewHorarios, viewHistorialEmpleados
        };
        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML private void mostrarRegistroEmpleado() { ocultarTodas(); viewRegistroEmpleado.setVisible(true); viewRegistroEmpleado.setManaged(true); }
    @FXML private void mostrarNomina() { ocultarTodas(); viewNomina.setVisible(true); viewNomina.setManaged(true); }
    @FXML private void mostrarUsuariosSistema() { ocultarTodas(); viewUsuariosSistema.setVisible(true); viewUsuariosSistema.setManaged(true); }
    @FXML private void mostrarRolesPermisos() { ocultarTodas(); viewRolesPermisos.setVisible(true); viewRolesPermisos.setManaged(true); }
    @FXML private void mostrarAsistencia() { ocultarTodas(); viewAsistencia.setVisible(true); viewAsistencia.setManaged(true); }
    @FXML private void mostrarHorarios() { ocultarTodas(); viewHorarios.setVisible(true); viewHorarios.setManaged(true); }
    @FXML private void mostrarHistorialEmpleados() { ocultarTodas(); viewHistorialEmpleados.setVisible(true); viewHistorialEmpleados.setManaged(true); }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void guardarRegistroEmpleado() {
        String nombre = txtNombreEmpleado != null ? txtNombreEmpleado.getText().trim() : "";
        String apellido = txtApellidoEmpleado != null ? txtApellidoEmpleado.getText().trim() : "";
        String cedula = txtCedulaEmpleado != null ? txtCedulaEmpleado.getText().trim() : "";

        if (nombre.isEmpty() || apellido.isEmpty() || cedula.isEmpty()) {
            Alertas.advertencia("Validación", "Complete al menos nombre, apellido y cédula.");
            return;
        }

        boolean exito = EmpleadoDAO.guardarEmpleado(
                nombre,
                apellido,
                cedula,
                txtTelefonoEmpleado != null ? txtTelefonoEmpleado.getText().trim() : "",
                txtCorreoEmpleado != null ? txtCorreoEmpleado.getText().trim() : "",
                txtDireccionEmpleado != null ? txtDireccionEmpleado.getText().trim() : ""
        );

        if (exito) {
            Alertas.exito("Empleado", "Empleado guardado correctamente en SQL Server.");
            limpiarRegistroEmpleado();
            cargarEmpleados();
        } else {
            Alertas.error("Empleado", "No se pudo guardar. Verifique tbl_EMPLEADO en la base de datos.");
        }
    }

    @FXML
    private void limpiarRegistroEmpleado() {
        if (txtNombreEmpleado != null) txtNombreEmpleado.clear();
        if (txtApellidoEmpleado != null) txtApellidoEmpleado.clear();
        if (txtCedulaEmpleado != null) txtCedulaEmpleado.clear();
        if (txtTelefonoEmpleado != null) txtTelefonoEmpleado.clear();
        if (txtCorreoEmpleado != null) txtCorreoEmpleado.clear();
        if (txtDireccionEmpleado != null) txtDireccionEmpleado.clear();
    }

    private void cargarEmpleados() {
        if (tablaEmpleados != null) {
            tablaEmpleados.setItems(FXCollections.observableArrayList(EmpleadoDAO.listar()));
        }
    }
}
