package org.example.salsiaopf.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.salsiaopf.dao.MantenimientoDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MantenimientoController {

    @FXML private ImageView logoImage;

    @FXML private VBox viewEquipos;
    @FXML private VBox viewFallos;
    @FXML private VBox viewReparaciones;
    @FXML private VBox viewLimpieza;
    @FXML private VBox viewHistorialMantenimiento;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;


    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarEquipos();
        animarEntrada();
    }

    private void animarEntrada() {
        ControllerUtil.animarEntrada(lblFechaActual);
    }

    private void cargarLogo() {
        ControllerUtil.cargarLogo(logoImage);
    }
    private void iniciarReloj() {
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.informacion("Notificaciones", "No hay notificaciones pendientes de mantenimiento.");
    }

    private void ocultarTodas() {
        VBox[] vistas = {
                viewEquipos,
                viewFallos,
                viewReparaciones,
                viewLimpieza,
                viewHistorialMantenimiento
        };

        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML
    private void mostrarEquipos() {
        ocultarTodas();
        viewEquipos.setVisible(true);
        viewEquipos.setManaged(true);
    }

    @FXML
    private void mostrarFallos() {
        ocultarTodas();
        viewFallos.setVisible(true);
        viewFallos.setManaged(true);
    }

    @FXML
    private void mostrarReparaciones() {
        ocultarTodas();
        viewReparaciones.setVisible(true);
        viewReparaciones.setManaged(true);
    }

    @FXML
    private void mostrarLimpieza() {
        ocultarTodas();
        viewLimpieza.setVisible(true);
        viewLimpieza.setManaged(true);
    }

    @FXML
    private void mostrarHistorialMantenimiento() {
        ocultarTodas();
        viewHistorialMantenimiento.setVisible(true);
        viewHistorialMantenimiento.setManaged(true);
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }

    // ── Equipos ──────────────────────────────────────────────────────
    @FXML private void nuevoEquipo() {
        Alertas.informacion("Nuevo equipo", "Formulario listo para registrar un nuevo equipo.");
    }

    @FXML private void editarEquipo() {
        Alertas.informacion("Editar equipo", "Selecciona un equipo de la tabla para editar.");
    }

    @FXML private void eliminarEquipo() {
        Alertas.advertencia("Eliminar equipo", "¿Seguro de eliminar este equipo? Esta acción no se puede deshacer.");
    }

    @FXML private void guardarEquipo() {
        if (MantenimientoDAO.guardarMantenimiento("Equipo", "Registro guardado desde interfaz")) {
            Alertas.exito("Equipo", "Equipo guardado correctamente.");
        } else {
            Alertas.error("Equipo", "No se pudo guardar. Verifique tbl_MANTENIMIENTO en la base de datos.");
        }
    }

    @FXML private void buscarEquipo() {
        Alertas.informacion("Buscar equipo", "Función de búsqueda próxima a implementar.");
    }

    @FXML private void verHistorialEquipo() {
        Alertas.informacion("Historial equipo", "Próximamente: historial completo del equipo seleccionado.");
    }

    // ── Fallos ───────────────────────────────────────────────────────
    @FXML private void registrarFallo() {
        Alertas.informacion("Registrar fallo", "Formulario listo para registrar un nuevo fallo.");
    }

    @FXML private void editarFallo() {
        Alertas.informacion("Editar fallo", "Selecciona un fallo de la tabla para editar.");
    }

    @FXML private void resolverFallo() {
        Alertas.informacion("Resolver fallo", "Marca el fallo seleccionado como resuelto.");
    }

    @FXML private void eliminarFallo() {
        Alertas.advertencia("Eliminar fallo", "¿Seguro de eliminar este fallo del registro?");
    }

    @FXML private void buscarFallo() {
        Alertas.informacion("Buscar fallo", "Función de búsqueda próxima a implementar.");
    }

    @FXML private void generarReporteFallos() {
        Alertas.informacion("Reporte de fallos", "Próximamente: generación de reporte PDF de fallos.");
    }

    // ── Reparaciones ─────────────────────────────────────────────────
    @FXML private void nuevaReparacion() {
        Alertas.informacion("Nueva reparación", "Formulario listo para registrar una reparación.");
    }

    @FXML private void editarReparacion() {
        Alertas.informacion("Editar reparación", "Selecciona una reparación de la tabla para editar.");
    }

    @FXML private void finalizarReparacion() {
        Alertas.exito("Reparación", "Reparación marcada como finalizada.");
    }

    @FXML private void eliminarReparacion() {
        Alertas.advertencia("Eliminar reparación", "¿Seguro de eliminar esta reparación?");
    }

    @FXML private void buscarReparacion() {
        Alertas.informacion("Buscar reparación", "Función de búsqueda próxima a implementar.");
    }

    @FXML private void generarOrdenTecnica() {
        Alertas.informacion("Orden técnica", "Próximamente: generación de orden técnica PDF.");
    }

    // ── Limpieza ─────────────────────────────────────────────────────
    @FXML private void nuevaTareaLimpieza() {
        Alertas.informacion("Nueva tarea", "Formulario listo para registrar una tarea de limpieza.");
    }

    @FXML private void editarTareaLimpieza() {
        Alertas.informacion("Editar tarea", "Selecciona una tarea de la tabla para editar.");
    }

    @FXML private void completarTareaLimpieza() {
        Alertas.exito("Limpieza", "Tarea marcada como completada.");
    }

    @FXML private void eliminarTareaLimpieza() {
        Alertas.advertencia("Eliminar tarea", "¿Seguro de eliminar esta tarea de limpieza?");
    }

    @FXML private void buscarTareaLimpieza() {
        Alertas.informacion("Buscar tarea", "Función de búsqueda próxima a implementar.");
    }

    @FXML private void generarRutinaDiaria() {
        Alertas.informacion("Rutina diaria", "Próximamente: generación de rutina de limpieza diaria.");
    }

    // ── Historial ────────────────────────────────────────────────────
    @FXML private void exportarHistorial() {
        Alertas.informacion("Exportar historial", "Próximamente: exportación a Excel y PDF.");
    }

    @FXML private void imprimirHistorial() {
        Alertas.informacion("Imprimir historial", "Próximamente: impresión de historial.");
    }

    @FXML private void actualizarHistorial() {
        Alertas.informacion("Historial", "Vista de historial actualizada.");
    }

    @FXML private void buscarHistorial() {
        Alertas.informacion("Buscar historial", "Función de búsqueda próxima a implementar.");
    }
}