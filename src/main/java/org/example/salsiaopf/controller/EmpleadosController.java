package org.example.salsiaopf.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.salsiaopf.dao.EmpleadoDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EmpleadosController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private StackPane contenedorEmpleados;

    // ScrollPanes
    @FXML private ScrollPane scrollDashboard, scrollRegistrar, scrollCargos, scrollAsistencia, scrollTurnos, scrollUsuarios, scrollHistorial;

    // Dashboard
    @FXML private Label lblActivos, lblInactivos, lblAsistenciasHoy, lblTotalEmpleados;
    @FXML private TextField txtBuscarEmpleado;
    @FXML private ComboBox<String> cmbFiltroCargo, cmbFiltroEstado;
    @FXML private TableView<Object[]> tablaEmpleados;

    // Registrar
    @FXML private TextField txtCodigo, txtCedula, txtNombre, txtApellido, txtTelefono, txtCorreo, txtDireccion, txtSueldo;
    @FXML private ComboBox<String> cmbCargo, cmbArea, cmbTurno, cmbEstado;
    @FXML private DatePicker dateIngreso;
    @FXML private TextArea txtObservacion;
    @FXML private TableView<Object[]> tablaRegistroEmpleados;

    // Cargos
    @FXML private TextField txtIdCargo, txtNombreCargo, txtBuscarCargo;
    @FXML private ComboBox<String> cmbEstadoCargo;
    @FXML private TextArea txtDescCargo;
    @FXML private TableView<Object[]> tablaCargos;

    // Asistencia
    @FXML private Label lblPresentesHoy, lblTardanzasHoy, lblAusentesHoy, lblTotalAsistencias;
    @FXML private DatePicker dateFiltroAsistencia;
    @FXML private ComboBox<String> cmbFiltroEstadoAsistencia;
    @FXML private TextField txtBuscarAsistencia;
    @FXML private TableView<Object[]> tablaAsistencia;

    // Turnos
    @FXML private ComboBox<String> cmbEmpleadoTurno, cmbDiaTurno, cmbFiltroDiaTurno;
    @FXML private TextField txtHoraEntradaTurno, txtHoraSalidaTurno, txtBuscarTurno;
    @FXML private TextArea txtObsTurno;
    @FXML private TableView<Object[]> tablaTurnos;

    // Usuarios
    @FXML private TextField txtIdUsuario, txtNombreUsuario, txtPermisosUsuario, txtBuscarUsuario;
    @FXML private ComboBox<String> cmbEmpleadoUsuario, cmbRolUsuario, cmbEstadoUsuario;
    @FXML private PasswordField txtPasswordUsuario, txtConfirmarPassword;
    @FXML private TableView<Object[]> tablaUsuarios;

    // Historial
    @FXML private Label lblTotalEventos, lblCambiosCargo, lblHistorialAsistencias, lblIncidencias;
    @FXML private DatePicker dateHistorialDesde, dateHistorialHasta;
    @FXML private ComboBox<String> cmbFiltroTipoEvento;
    @FXML private TextField txtBuscarHistorial;
    @FXML private TableView<Object[]> tablaHistorial;

    private ObservableList<Object[]> empleadosData = FXCollections.observableArrayList();
    private ObservableList<Object[]> cargosData = FXCollections.observableArrayList();
    private ObservableList<Object[]> asistenciasData = FXCollections.observableArrayList();
    private ObservableList<Object[]> turnosData = FXCollections.observableArrayList();
    private ObservableList<Object[]> usuariosData = FXCollections.observableArrayList();
    private ObservableList<Object[]> historialData = FXCollections.observableArrayList();

    private int editandoId = -1;
    private int editandoCargoId = -1;
    private int editandoTurnoId = -1;
    private int editandoUsuarioId = -1;

    @FXML
    private void initialize() {
        ControllerUtil.cargarLogo(logoImage);
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
        EmpleadoDAO.crearTablasSiNoExisten();

        cargarCombos();
        configurarTablaEmpleados();
        configurarTablaRegistroEmpleados();
        configurarTablaCargos();
        cargarTablaCargos();
        configurarTablaAsistencia();
        configurarTablaTurnos();
        configurarTablaUsuarios();
        configurarTablaHistorial();
        mostrarDashboard();
        cargarDashboard();
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVEGACIÓN
    // ═══════════════════════════════════════════════════════════════

    private void ocultarTodos() {
        ScrollPane[] panes = { scrollDashboard, scrollRegistrar, scrollCargos, scrollAsistencia, scrollTurnos, scrollUsuarios, scrollHistorial };
        for (ScrollPane sp : panes) {
            if (sp != null) { sp.setVisible(false); sp.setManaged(false); }
        }
    }

    private void mostrarScroll(ScrollPane sp) {
        ocultarTodos();
        if (sp != null) { sp.setVisible(true); sp.setManaged(true); }
    }

    @FXML private void mostrarDashboard() { mostrarScroll(scrollDashboard); cargarDashboard(); }
    @FXML private void mostrarRegistrar() { mostrarScroll(scrollRegistrar); cargarCombos(); cargarTablaRegistroEmpleados(); }
    @FXML private void mostrarCargos() { mostrarScroll(scrollCargos); cargarTablaCargos(); }
    @FXML private void mostrarAsistencia() { mostrarScroll(scrollAsistencia); cargarTablaAsistencia(); actualizarMetricasAsistencia(); }
    @FXML private void mostrarTurnos() { mostrarScroll(scrollTurnos); cargarCombos(); cargarTablaTurnos(); }
    @FXML private void mostrarUsuarios() {
        TextInputDialog pw = new TextInputDialog();
        pw.setTitle("Acceso restringido");
        pw.setHeaderText("Ingrese la clave para acceder a Usuarios");
        pw.setContentText("Clave:");
        PasswordField pf = new PasswordField();
        pf.setPrefWidth(250);
        pw.getDialogPane().setContent(pf);
        pw.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        pw.setResultConverter(btn -> btn == ButtonType.OK ? pf.getText() : null);
        String clave = pw.showAndWait().orElse(null);
        if (!"010929".equals(clave)) { Alertas.error("Acceso denegado", "Clave incorrecta."); return; }
        mostrarScroll(scrollUsuarios); cargarCombos(); cargarTablaUsuarios();
    }
    @FXML private void mostrarHistorial() { mostrarScroll(scrollHistorial); cargarTablaHistorial(); actualizarMetricasHistorial(); }

    @FXML private void volverMenu(ActionEvent e) { Navegacion.volverCentroSistema(e); }
    @FXML private void salirSistema() { System.exit(0); }
    @FXML private void mostrarNotificaciones() { Alertas.advertencia("Notificaciones", "En desarrollo."); }
    @FXML private void irARegistrarAsistencia() { mostrarAsistencia(); }
    @FXML private void irATurnos() { mostrarTurnos(); }
    @FXML private void volverADashboard() { mostrarDashboard(); }

    // ═══════════════════════════════════════════════════════════════
    //  CARGAR COMBOS
    // ═══════════════════════════════════════════════════════════════

    private void cargarCombos() {
        // Cargos
        List<String> cargos = EmpleadoDAO.obtenerNombresCargos();
        if (cmbCargo != null) { cmbCargo.getItems().setAll(cargos); }
        if (cmbFiltroCargo != null) { cmbFiltroCargo.getItems().setAll(FXCollections.observableArrayList("Todos")); cmbFiltroCargo.getItems().addAll(cargos); cmbFiltroCargo.setValue("Todos"); }

        // Áreas
        String[] areas = {"Cocina", "Panadería", "Limpieza", "Administración", "Ventas", "Almacén", "Delivery", "Atención al cliente"};
        if (cmbArea != null) cmbArea.getItems().setAll(areas);

        // Turnos
        String[] turnos = {"Mañana (8AM-4PM)", "Tarde (4PM-12AM)", "Noche (12AM-8AM)", "Completo", "Part-time"};
        if (cmbTurno != null) cmbTurno.getItems().setAll(turnos);

        // Estados
        String[] estados = {"Activo", "Inactivo", "Suspendido", "Vacaciones"};
        if (cmbEstado != null) cmbEstado.getItems().setAll(estados);
        if (cmbEstadoCargo != null) cmbEstadoCargo.getItems().setAll("Activo", "Inactivo");
        if (cmbEstadoUsuario != null) cmbEstadoUsuario.getItems().setAll("Activo", "Bloqueado", "Inactivo");
        if (cmbFiltroEstado != null) { cmbFiltroEstado.getItems().setAll("Todos", "Activo", "Inactivo", "Suspendido", "Vacaciones"); cmbFiltroEstado.setValue("Todos"); }

        // Días
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        if (cmbDiaTurno != null) cmbDiaTurno.getItems().setAll(dias);
        if (cmbFiltroDiaTurno != null) { cmbFiltroDiaTurno.getItems().setAll("Todos"); cmbFiltroDiaTurno.getItems().addAll(dias); cmbFiltroDiaTurno.setValue("Todos"); }

        // Empleados activos (for turnos & usuarios)
        String[] emps = cargarEmpleadosCombo();
        if (cmbEmpleadoTurno != null) cmbEmpleadoTurno.getItems().setAll(emps);
        if (cmbEmpleadoUsuario != null) { cmbEmpleadoUsuario.getItems().setAll(emps); }

        // Roles usuarios
        String[] roles = {"Administrador", "Cajero", "Cocinero", "Delivery", "Inventario", "Supervisor", "Limpieza"};
        if (cmbRolUsuario != null) cmbRolUsuario.getItems().setAll(roles);

        // Filtro estado asistencia
        if (cmbFiltroEstadoAsistencia != null) { cmbFiltroEstadoAsistencia.getItems().setAll("Todos", "Presente", "Ausente", "Tarde"); cmbFiltroEstadoAsistencia.setValue("Todos"); }

        // Filtro tipo evento historial
        String[] eventos = {"Todos", "Alta", "Cambio de cargo", "Cambio de turno", "Permiso", "Ausencia", "Suspensión", "Reactivación", "Inactivación", "Asistencia"};
        if (cmbFiltroTipoEvento != null) { cmbFiltroTipoEvento.getItems().setAll(eventos); cmbFiltroTipoEvento.setValue("Todos"); }
    }

    private String[] cargarEmpleadosCombo() {
        List<String> lista = EmpleadoDAO.obtenerNombresEmpleadosActivos();
        return lista.toArray(new String[0]);
    }

    // ═══════════════════════════════════════════════════════════════
    //  CONFIGURAR TABLAS
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private <T> void configurarColumna(TableView<T> table, int idx) {
        if (table.getColumns().size() <= idx) return;
    }

    private void configurarTablaEmpleados() {
        if (tablaEmpleados == null) return;
        tablaEmpleados.setItems(empleadosData);
        for (int i = 0; i < 8; i++) {
            TableColumn<Object[], Object> col = (TableColumn<Object[], Object>) tablaEmpleados.getColumns().get(i);
            final int idx = i;
            col.setCellValueFactory(cd -> {
                Object[] row = cd.getValue();
                if (row == null || row.length <= idx || row[idx] == null) return new SimpleObjectProperty<>("");
                Object val = row[idx];
                if (idx == 6) {
                    String est = val.toString();
                    if ("Activo".equals(est)) return new SimpleObjectProperty<>("✅ Activo");
                    if ("Inactivo".equals(est)) return new SimpleObjectProperty<>("❌ Inactivo");
                    return new SimpleObjectProperty<>(est);
                }
                if (idx == 5) return new SimpleObjectProperty<>(val.toString());
                return new SimpleObjectProperty<>(val.toString());
            });
        }
        TableColumn<Object[], Void> colAcc = (TableColumn<Object[], Void>) tablaEmpleados.getColumns().get(8);
        colAcc.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✎"), btnVer = new Button("👁"), btnElim = new Button("🗑");
            { btnEdit.getStyleClass().addAll("cellActionBtn", "actionOrange");
              btnVer.getStyleClass().addAll("cellActionBtn", "actionBlue");
              btnElim.getStyleClass().addAll("cellActionBtn", "actionRed"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Object[] r = getTableView().getItems().get(getIndex());
                btnEdit.setOnAction(e -> editarEmpleado((int)r[0]));
                btnVer.setOnAction(e -> verDetalleEmpleado((int)r[0]));
                btnElim.setOnAction(e -> eliminarEmpleado((int)r[0]));
                setGraphic(new HBox(4, btnEdit, btnVer, btnElim));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void configurarTablaGenerica(TableView<Object[]> table, ObservableList<Object[]> data, int numCols, java.util.function.BiConsumer<Integer, Object[]> actionHandler) {
        if (table == null) return;
        table.setItems(data);
        for (int i = 0; i < numCols; i++) {
            TableColumn<Object[], Object> col = (TableColumn<Object[], Object>) table.getColumns().get(i);
            final int idx = i;
            col.setCellValueFactory(cd -> {
                Object[] row = cd.getValue();
                if (row == null || row.length <= idx || row[idx] == null) return new SimpleObjectProperty<>("");
                Object val = row[idx];
                if (val instanceof LocalDate) return new SimpleObjectProperty<>(((LocalDate)val).toString());
                return new SimpleObjectProperty<>(val.toString());
            });
        }
        if (actionHandler != null && table.getColumns().size() > numCols) {
            TableColumn<Object[], Void> colAcc = (TableColumn<Object[], Void>) table.getColumns().get(numCols);
            colAcc.setCellFactory(col -> new TableCell<>() {
                private final Button btnEdit = new Button("✎"), btnElim = new Button("🗑");
                { btnEdit.getStyleClass().addAll("cellActionBtn", "actionOrange");
                  btnElim.getStyleClass().addAll("cellActionBtn", "actionRed"); }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Object[] r = getTableView().getItems().get(getIndex());
                    int id = (int)r[0];
                    btnEdit.setOnAction(e -> actionHandler.accept(id, r));
                    btnElim.setOnAction(e -> {
                        if (Alertas.confirmar("Eliminar", "¿Eliminar este registro?")) {
                            actionHandler.accept(-id, r);
                        }
                    });
                    setGraphic(new HBox(4, btnEdit, btnElim));
                }
            });
        }
    }

    private void configurarTablaRegistroEmpleados() {
        if (tablaRegistroEmpleados == null) return;
        configurarTablaGenerica(tablaRegistroEmpleados, FXCollections.observableArrayList(), 6, (id, row) -> {
            if (id > 0) editarEmpleado(id);
            else eliminarEmpleado(-id);
        });
    }

    private void configurarTablaAsistencia() {
        if (tablaAsistencia == null) return;
        configurarTablaGenerica(tablaAsistencia, asistenciasData, 7, null);
    }

    private void configurarTablaTurnos() {
        if (tablaTurnos == null) return;
        configurarTablaGenerica(tablaTurnos, turnosData, 7, (id, row) -> {
            if (id > 0) { editandoTurnoId = id;
                String empStr = (String)row[1];
                for (String e : cmbEmpleadoTurno.getItems()) { if (e.contains(empStr.split(" ")[0])) { cmbEmpleadoTurno.setValue(e); break; } }
                cmbDiaTurno.setValue((String)row[3]); txtHoraEntradaTurno.setText((String)row[4]);
                txtHoraSalidaTurno.setText((String)row[5]); }
            else eliminarTurno(id);
        });
    }

    private void configurarTablaUsuarios() {
        if (tablaUsuarios == null) return;
        configurarTablaGenerica(tablaUsuarios, usuariosData, 6, (id, row) -> {
            if (id > 0) { editandoUsuarioId = id; txtIdUsuario.setText(String.valueOf(id));
                String empStr = (String)row[1];
                for (String e : cmbEmpleadoUsuario.getItems()) { if (e.contains(empStr.split(" ")[0])) { cmbEmpleadoUsuario.setValue(e); break; } }
                txtNombreUsuario.setText((String)row[2]); cmbRolUsuario.setValue((String)row[3]);
                cmbEstadoUsuario.setValue((String)row[4]); }
            else eliminarUsuario(id);
        });
    }

    private void configurarTablaHistorial() {
        if (tablaHistorial == null) return;
        configurarTablaGenerica(tablaHistorial, historialData, 7, null);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════════════

    private void cargarDashboard() {
        lblActivos.setText(String.valueOf(EmpleadoDAO.contarActivos()));
        lblInactivos.setText(String.valueOf(EmpleadoDAO.contarInactivos()));
        lblAsistenciasHoy.setText(String.valueOf(EmpleadoDAO.contarAsistenciasHoy()));
        cargarTablaEmpleados();
    }

    private void cargarTablaEmpleados() {
        empleadosData.setAll(EmpleadoDAO.listarEmpleados());
        lblTotalEmpleados.setText(String.valueOf(empleadosData.size()));
    }

    @FXML private void filtrarEmpleados() {
        String q = txtBuscarEmpleado != null ? txtBuscarEmpleado.getText().toLowerCase() : "";
        String cargoF = cmbFiltroCargo != null ? cmbFiltroCargo.getValue() : "Todos";
        String estadoF = cmbFiltroEstado != null ? cmbFiltroEstado.getValue() : "Todos";
        List<Object[]> filtrados = EmpleadoDAO.listarEmpleados().stream().filter(r -> {
            String nombre = r[2] + " " + r[3]; String cedula = (String)r[4]; String cargo = (String)r[8]; String est = (String)r[12];
            boolean matchQ = q.isEmpty() || nombre.toLowerCase().contains(q) || cedula.toLowerCase().contains(q) || (cargo != null && cargo.toLowerCase().contains(q));
            boolean matchCargo = "Todos".equals(cargoF) || (cargo != null && cargo.equals(cargoF));
            boolean matchEstado = "Todos".equals(estadoF) || (est != null && est.equals(estadoF));
            return matchQ && matchCargo && matchEstado;
        }).collect(Collectors.toList());
        empleadosData.setAll(filtrados);
    }

    @FXML private void limpiarDashboard() {
        if (txtBuscarEmpleado != null) txtBuscarEmpleado.clear();
        if (cmbFiltroCargo != null) cmbFiltroCargo.setValue("Todos");
        if (cmbFiltroEstado != null) cmbFiltroEstado.setValue("Todos");
        cargarTablaEmpleados();
    }

    @FXML private void exportarEmpleados() {
        Alertas.informacion("Exportar", "Función de exportación próximamente.");
    }

    @FXML private void nuevoEmpleado() {
        editandoId = -1;
        limpiarFormulario();
        mostrarRegistrar();
    }

    private void verDetalleEmpleado(int id) {
        Object[] emp = EmpleadoDAO.obtenerEmpleado(id);
        if (emp == null) { Alertas.error("Error", "Empleado no encontrado."); return; }
        String msg = "Código: " + emp[1] + "\nNombre: " + emp[2] + " " + emp[3] + "\nCédula: " + emp[4] +
                     "\nTeléfono: " + emp[5] + "\nDirección: " + emp[6] + "\nCorreo: " + emp[7] +
                     "\nCargo: " + emp[8] + "\nÁrea: " + emp[9] + "\nTurno: " + emp[10] +
                     "\nSueldo: RD$ " + String.format("%.2f", (double)emp[11]) + "\nEstado: " + emp[12] +
                     "\nF. Ingreso: " + (emp[13] != null ? emp[13] : "N/A");
        Alertas.informacion("Detalle del empleado", msg);
    }

    // ═══════════════════════════════════════════════════════════════
    //  REGISTRAR / EDITAR EMPLEADO
    // ═══════════════════════════════════════════════════════════════

    private void editarEmpleado(int id) {
        Object[] emp = EmpleadoDAO.obtenerEmpleado(id);
        if (emp == null) { Alertas.error("Error", "No se encontró el empleado."); return; }
        editandoId = id;
        txtCodigo.setText((String)emp[1]); txtNombre.setText((String)emp[2]); txtApellido.setText((String)emp[3]);
        txtCedula.setText((String)emp[4]); txtTelefono.setText((String)emp[5]); txtDireccion.setText((String)emp[6]);
        txtCorreo.setText((String)emp[7]);
        cmbCargo.setValue((String)emp[8]); cmbArea.setValue((String)emp[9]); cmbTurno.setValue((String)emp[10]);
        txtSueldo.setText(String.format("%.2f", (double)emp[11]));
        cmbEstado.setValue((String)emp[12]);
        dateIngreso.setValue((LocalDate)emp[13]);
        txtObservacion.setText((String)emp[14]);
        mostrarRegistrar();
    }

    @FXML private void guardarEmpleado() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String cedula = txtCedula.getText().trim();
        if (nombre.isEmpty() || apellido.isEmpty() || cedula.isEmpty()) {
            Alertas.advertencia("Validación", "Nombre, apellido y cédula son obligatorios."); return;
        }
        double sueldo = 0;
        try { if (!txtSueldo.getText().trim().isEmpty()) sueldo = Double.parseDouble(txtSueldo.getText().trim()); }
        catch (NumberFormatException e) { Alertas.advertencia("Validación", "Sueldo inválido."); return; }

        boolean ok;
        if (editandoId > 0) {
            ok = EmpleadoDAO.actualizarEmpleado(editandoId, nombre, apellido, cedula, txtTelefono.getText().trim(),
                    txtDireccion.getText().trim(), txtCorreo.getText().trim(),
                    cmbCargo.getValue() != null ? cmbCargo.getValue() : "",
                    cmbArea.getValue() != null ? cmbArea.getValue() : "",
                    cmbTurno.getValue() != null ? cmbTurno.getValue() : "",
                    dateIngreso.getValue(), sueldo,
                    cmbEstado.getValue() != null ? cmbEstado.getValue() : "Activo",
                    txtObservacion.getText().trim());
            if (ok) { EmpleadoDAO.guardarHistorial(editandoId, "Cambio de datos", "Datos actualizados", "Admin", "Empleados"); }
        } else {
            int id = EmpleadoDAO.guardarEmpleado(nombre, apellido, cedula, txtTelefono.getText().trim(),
                    txtDireccion.getText().trim(), txtCorreo.getText().trim(),
                    cmbCargo.getValue() != null ? cmbCargo.getValue() : "",
                    cmbArea.getValue() != null ? cmbArea.getValue() : "",
                    cmbTurno.getValue() != null ? cmbTurno.getValue() : "",
                    dateIngreso.getValue(), sueldo,
                    cmbEstado.getValue() != null ? cmbEstado.getValue() : "Activo",
                    txtObservacion.getText().trim());
            ok = id > 0;
            if (ok) { EmpleadoDAO.guardarHistorial(id, "Alta", "Nuevo empleado registrado", "Admin", "Empleados"); }
        }
        if (ok) {
            Alertas.exito("Empleado", editandoId > 0 ? "Empleado actualizado." : "Empleado guardado.");
            limpiarFormulario();
            cargarTablaEmpleados();
            cargarTablaRegistroEmpleados();
            editandoId = -1;
        } else {
            Alertas.error("Error", "No se pudo guardar. Verifique cédula única.");
        }
    }

    @FXML private void limpiarFormulario() {
        txtCodigo.clear(); txtNombre.clear(); txtApellido.clear(); txtCedula.clear(); txtTelefono.clear();
        txtDireccion.clear(); txtCorreo.clear(); txtSueldo.clear(); txtObservacion.clear();
        cmbCargo.setValue(null); cmbArea.setValue(null); cmbTurno.setValue(null); cmbEstado.setValue("Activo");
        dateIngreso.setValue(null); editandoId = -1;
    }

    @FXML private void cancelarEdicion() {
        limpiarFormulario(); mostrarDashboard();
    }

    private void eliminarEmpleado(int id) {
        if (Alertas.confirmar("Eliminar empleado", "¿Está seguro de eliminar este empleado?")) {
            if (EmpleadoDAO.eliminarEmpleado(id)) {
                EmpleadoDAO.guardarHistorial(id, "Inactivación", "Empleado eliminado del sistema", "Admin", "Empleados");
                Alertas.exito("Eliminado", "Empleado eliminado.");
                cargarTablaEmpleados(); cargarTablaRegistroEmpleados();
            } else Alertas.error("Error", "No se pudo eliminar.");
        }
    }

    private void cargarTablaRegistroEmpleados() {
        if (tablaRegistroEmpleados != null) {
            ObservableList<Object[]> data = FXCollections.observableArrayList();
            for (Object[] emp : EmpleadoDAO.listarEmpleados()) {
                data.add(new Object[]{ emp[0], emp[1], emp[2] + " " + emp[3], emp[4], emp[8], emp[12] });
            }
            tablaRegistroEmpleados.setItems(data);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARGOS
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void configurarTablaCargos() {
        if (tablaCargos == null) return;
        tablaCargos.setItems(cargosData);
        int ncol = tablaCargos.getColumns().size();
        for (int i = 0; i < Math.min(5, ncol); i++) {
            TableColumn<Object[], Object> col = (TableColumn<Object[], Object>) tablaCargos.getColumns().get(i);
            final int idx = i;
            col.setCellValueFactory(cd -> {
                Object[] row = cd.getValue();
                if (row == null || row.length <= idx || row[idx] == null) return new SimpleObjectProperty<>("");
                return new SimpleObjectProperty<>(row[idx].toString());
            });
        }
        if (ncol > 5) {
            TableColumn<Object[], Void> colAcc = (TableColumn<Object[], Void>) tablaCargos.getColumns().get(5);
            colAcc.setCellFactory(col -> new TableCell<>() {
                private final Button btnEdit = new Button("✎"), btnElim = new Button("🗑");
                { btnEdit.getStyleClass().addAll("cellActionBtn","actionOrange"); btnElim.getStyleClass().addAll("cellActionBtn","actionRed"); }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Object[] r = getTableView().getItems().get(getIndex());
                    if (r == null || r.length < 3) return;
                    btnEdit.setOnAction(e -> {
                        editandoCargoId = (int)r[0]; txtIdCargo.setText(r[0].toString());
                        txtNombreCargo.setText((String)r[1]); txtDescCargo.setText((String)r[2]);
                    });
                    btnElim.setOnAction(e -> {
                        if (Alertas.confirmar("Eliminar","¿Eliminar este cargo?")) {
                            if (EmpleadoDAO.eliminarCargo((int)r[0])) { Alertas.exito("Eliminado",""); cargarTablaCargos(); cargarCombos(); }
                            else Alertas.error("Error","No se pudo eliminar.");
                        }
                    });
                    setGraphic(new HBox(4, btnEdit, btnElim));
                }
            });
        }
    }

    private void cargarTablaCargos() {
        try {
            java.util.List<Object[]> lista = EmpleadoDAO.listarCargos();
            cargosData.setAll(lista);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void nuevoCargo() {
        editandoCargoId = -1; limpiarFormCargo();
    }

    @FXML private void guardarCargo() {
        String nombre = txtNombreCargo.getText().trim();
        if (nombre.isEmpty()) { Alertas.advertencia("Validación", "El nombre del cargo es obligatorio."); return; }
        boolean ok;
        if (editandoCargoId > 0) {
            ok = EmpleadoDAO.actualizarCargo(editandoCargoId, nombre, txtDescCargo.getText().trim());
        } else {
            ok = EmpleadoDAO.guardarCargo(nombre, txtDescCargo.getText().trim()) > 0;
        }
        if (ok) {
            Alertas.exito("Cargo", editandoCargoId > 0 ? "Cargo actualizado." : "Cargo guardado.");
            limpiarFormCargo(); cargarTablaCargos(); cargarCombos();
        } else Alertas.error("Error", "No se pudo guardar. Verifique que el nombre no esté duplicado.");
    }

    @FXML private void limpiarFormCargo() {
        txtIdCargo.clear(); txtNombreCargo.clear(); txtDescCargo.clear(); cmbEstadoCargo.setValue("Activo"); editandoCargoId = -1;
    }

    private void eliminarCargo(int id) {
        if (EmpleadoDAO.eliminarCargo(-id)) { Alertas.exito("Eliminado", "Cargo eliminado."); cargarTablaCargos(); cargarCombos(); }
        else Alertas.error("Error", "No se pudo eliminar.");
    }

    @FXML private void filtrarCargos() {
        String q = txtBuscarCargo.getText().toLowerCase();
        if (q.isEmpty()) { cargarTablaCargos(); return; }
        List<Object[]> filtrados = EmpleadoDAO.listarCargos().stream()
            .filter(r -> r[1].toString().toLowerCase().contains(q) || r[2].toString().toLowerCase().contains(q))
            .collect(Collectors.toList());
        cargosData.setAll(filtrados);
    }

    @FXML private void exportarCargos() { Alertas.informacion("Exportar", "Próximamente."); }

    // ═══════════════════════════════════════════════════════════════
    //  ASISTENCIA
    // ═══════════════════════════════════════════════════════════════

    private void cargarTablaAsistencia() {
        asistenciasData.setAll(EmpleadoDAO.listarAsistencias());
    }

    private void actualizarMetricasAsistencia() {
        long presentes = asistenciasData.stream().filter(r -> "Presente".equals(r[6])).count();
        long tardes = asistenciasData.stream().filter(r -> "Tarde".equals(r[6])).count();
        long ausentes = asistenciasData.stream().filter(r -> "Ausente".equals(r[6])).count();
        lblPresentesHoy.setText(String.valueOf(presentes));
        lblTardanzasHoy.setText(String.valueOf(tardes));
        lblAusentesHoy.setText(String.valueOf(ausentes));
        lblTotalAsistencias.setText(String.valueOf(asistenciasData.size()));
    }

    @FXML private void filtrarAsistencias() {
        LocalDate fecha = dateFiltroAsistencia.getValue();
        String estado = cmbFiltroEstadoAsistencia.getValue();
        String q = txtBuscarAsistencia.getText().toLowerCase();
        List<Object[]> filtrados = EmpleadoDAO.listarAsistencias().stream().filter(r -> {
            boolean matchFecha = fecha == null || r[3].equals(fecha);
            boolean matchEstado = "Todos".equals(estado) || r[6].equals(estado);
            boolean matchQ = q.isEmpty() || r[2].toString().toLowerCase().contains(q);
            return matchFecha && matchEstado && matchQ;
        }).collect(Collectors.toList());
        asistenciasData.setAll(filtrados);
        actualizarMetricasAsistencia();
    }

    @FXML private void limpiarFiltrosAsistencia() {
        dateFiltroAsistencia.setValue(null);
        cmbFiltroEstadoAsistencia.setValue("Todos");
        txtBuscarAsistencia.clear();
        cargarTablaAsistencia();
        actualizarMetricasAsistencia();
    }

    @FXML private void exportarAsistencias() { Alertas.informacion("Exportar", "Próximamente."); }

    @FXML private void registrarEntrada() {
        SimpleStringProperty empProp = new SimpleStringProperty("");
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Registrar entrada");
        dialog.setHeaderText("Seleccione empleado para registrar entrada");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ComboBox<String> cmb = new ComboBox<>();
        cmb.getItems().setAll(cargarEmpleadosCombo());
        cmb.setPrefWidth(300);
        dialog.getDialogPane().setContent(cmb);
        Platform.runLater(cmb::requestFocus);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? new String[]{cmb.getValue()} : null);
        dialog.showAndWait().ifPresent(res -> {
            if (res[0] == null) return;
            String[] parts = res[0].split("\\|");
            int empId = Integer.parseInt(parts[0]);
            String nombre = parts[1];
            String hora = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
            if (EmpleadoDAO.guardarAsistencia(empId, LocalDate.now(), hora, "", "Presente", "") > 0) {
                EmpleadoDAO.guardarHistorial(empId, "Asistencia", "Entrada registrada: " + hora, "Admin", "Asistencia");
                Alertas.exito("Entrada", "Entrada registrada para " + nombre + " a las " + hora);
                cargarTablaAsistencia(); actualizarMetricasAsistencia();
            } else Alertas.error("Error", "No se pudo registrar entrada.");
        });
    }

    @FXML private void registrarSalida() {
        // Find latest attendance without exit time
        List<Object[]> asistencias = EmpleadoDAO.listarAsistencias();
        Object[] sinSalida = asistencias.stream().filter(r -> "Presente".equals(r[6]) && (r[5] == null || r[5].toString().isEmpty())).findFirst().orElse(null);
        if (sinSalida == null) { Alertas.advertencia("Sin pendientes", "No hay entradas sin salida registrada."); return; }
        String hora = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
        int empId = 0;
        String codigo = (String)sinSalida[1];
        List<Object[]> emps = EmpleadoDAO.listarEmpleados();
        for (Object[] e : emps) { if (codigo.equals(e[1])) { empId = (int)e[0]; break; } }
        if (empId == 0) return;
        // Update using raw SQL
        String sql = "UPDATE tbl_ASISTENCIA SET Hora_salida=? WHERE ID_asistencia=?";
        try (java.sql.Connection conn = org.example.salsiaopf.database.ConexionBD.conectar();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn != null) {
                ps.setString(1, hora);
                ps.setInt(2, (int)sinSalida[0]);
                if (ps.executeUpdate() > 0) {
                    EmpleadoDAO.guardarHistorial(empId, "Asistencia", "Salida registrada: " + hora, "Admin", "Asistencia");
                    Alertas.exito("Salida", "Salida registrada para " + sinSalida[2] + " a las " + hora);
                    cargarTablaAsistencia(); actualizarMetricasAsistencia();
                }
            }
        } catch (java.sql.SQLException e) { Alertas.error("Error", "No se pudo registrar salida."); }
    }

    @FXML private void marcarAusencia() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Marcar ausencia");
        dialog.setHeaderText("Seleccione empleado ausente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ComboBox<String> cmb = new ComboBox<>();
        cmb.getItems().setAll(cargarEmpleadosCombo());
        cmb.setPrefWidth(300);
        dialog.getDialogPane().setContent(cmb);
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? new String[]{cmb.getValue()} : null);
        dialog.showAndWait().ifPresent(res -> {
            if (res[0] == null) return;
            String[] parts = res[0].split("\\|");
            int empId = Integer.parseInt(parts[0]);
            if (EmpleadoDAO.guardarAsistencia(empId, LocalDate.now(), "", "", "Ausente", "Ausencia registrada") > 0) {
                EmpleadoDAO.guardarHistorial(empId, "Ausencia", "Ausencia registrada", "Admin", "Asistencia");
                Alertas.exito("Ausencia", "Ausencia registrada para " + parts[1]);
                cargarTablaAsistencia(); actualizarMetricasAsistencia();
            } else Alertas.error("Error", "No se pudo registrar.");
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  TURNOS
    // ═══════════════════════════════════════════════════════════════

    private void cargarTablaTurnos() { turnosData.setAll(EmpleadoDAO.listarTurnos()); }

    @FXML private void nuevoTurno() { editandoTurnoId = -1; limpiarFormTurno(); }

    @FXML private void guardarTurno() {
        if (cmbEmpleadoTurno.getValue() == null || cmbDiaTurno.getValue() == null ||
            txtHoraEntradaTurno.getText().trim().isEmpty() || txtHoraSalidaTurno.getText().trim().isEmpty()) {
            Alertas.advertencia("Validación", "Complete todos los campos obligatorios."); return;
        }
        String[] parts = cmbEmpleadoTurno.getValue().split("\\|");
        int empId = Integer.parseInt(parts[0]);
        boolean ok;
        if (editandoTurnoId > 0) {
            ok = EmpleadoDAO.actualizarTurno(editandoTurnoId, cmbDiaTurno.getValue(),
                    txtHoraEntradaTurno.getText().trim(), txtHoraSalidaTurno.getText().trim(), txtObsTurno.getText().trim());
        } else {
            ok = EmpleadoDAO.guardarTurno(empId, cmbDiaTurno.getValue(),
                    txtHoraEntradaTurno.getText().trim(), txtHoraSalidaTurno.getText().trim(), txtObsTurno.getText().trim()) > 0;
        }
        if (ok) {
            Alertas.exito("Turno", editandoTurnoId > 0 ? "Turno actualizado." : "Turno guardado.");
            if (editandoTurnoId <= 0) EmpleadoDAO.guardarHistorial(empId, "Cambio de turno", "Nuevo turno asignado: " + cmbDiaTurno.getValue() + " " + txtHoraEntradaTurno.getText().trim() + "-" + txtHoraSalidaTurno.getText().trim(), "Admin", "Turnos");
            limpiarFormTurno(); cargarTablaTurnos();
        } else Alertas.error("Error", "No se pudo guardar el turno.");
    }

    @FXML private void limpiarFormTurno() {
        cmbEmpleadoTurno.setValue(null); cmbDiaTurno.setValue(null);
        txtHoraEntradaTurno.clear(); txtHoraSalidaTurno.clear(); txtObsTurno.clear();
        editandoTurnoId = -1;
    }

    private void eliminarTurno(int id) {
        if (EmpleadoDAO.eliminarTurno(-id)) { Alertas.exito("Eliminado", "Turno eliminado."); cargarTablaTurnos(); }
        else Alertas.error("Error", "No se pudo eliminar.");
    }

    @FXML private void filtrarTurnos() {
        String q = txtBuscarTurno.getText().toLowerCase();
        String dia = cmbFiltroDiaTurno.getValue();
        List<Object[]> filtrados = EmpleadoDAO.listarTurnos().stream().filter(r -> {
            boolean matchQ = q.isEmpty() || r[2].toString().toLowerCase().contains(q) || r[3].toString().toLowerCase().contains(q);
            boolean matchDia = "Todos".equals(dia) || r[4].equals(dia);
            return matchQ && matchDia;
        }).collect(Collectors.toList());
        turnosData.setAll(filtrados);
    }

    // ═══════════════════════════════════════════════════════════════
    //  USUARIOS DEL SISTEMA
    // ═══════════════════════════════════════════════════════════════

    private void cargarTablaUsuarios() { usuariosData.setAll(EmpleadoDAO.listarUsuarios()); }

    @FXML private void nuevoUsuario() { editandoUsuarioId = -1; limpiarFormUsuario(); }

    @FXML private void guardarUsuario() {
        if (cmbEmpleadoUsuario.getValue() == null || txtNombreUsuario.getText().trim().isEmpty()) {
            Alertas.advertencia("Validación", "Empleado y nombre de usuario obligatorios."); return;
        }
        // Password required only for new
        if (editandoUsuarioId <= 0 && txtPasswordUsuario.getText().isEmpty()) {
            Alertas.advertencia("Validación", "Contraseña obligatoria para nuevo usuario."); return;
        }
        if (editandoUsuarioId <= 0 && !txtPasswordUsuario.getText().equals(txtConfirmarPassword.getText())) {
            Alertas.advertencia("Validación", "Las contraseñas no coinciden."); return;
        }
        String[] parts = cmbEmpleadoUsuario.getValue().split("\\|");
        int empId = Integer.parseInt(parts[0]);
        boolean ok;
        if (editandoUsuarioId > 0) {
            ok = EmpleadoDAO.actualizarUsuario(editandoUsuarioId, txtNombreUsuario.getText().trim(),
                    cmbRolUsuario.getValue() != null ? cmbRolUsuario.getValue() : "",
                    cmbEstadoUsuario.getValue() != null ? cmbEstadoUsuario.getValue() : "Activo",
                    txtPermisosUsuario.getText().trim());
        } else {
            ok = EmpleadoDAO.guardarUsuario(empId, txtNombreUsuario.getText().trim(),
                    txtPasswordUsuario.getText().trim(),
                    cmbRolUsuario.getValue() != null ? cmbRolUsuario.getValue() : "",
                    cmbEstadoUsuario.getValue() != null ? cmbEstadoUsuario.getValue() : "Activo",
                    txtPermisosUsuario.getText().trim()) > 0;
        }
        if (ok) {
            Alertas.exito("Usuario", editandoUsuarioId > 0 ? "Usuario actualizado." : "Usuario creado.");
            if (editandoUsuarioId <= 0) EmpleadoDAO.guardarHistorial(empId, "Usuario sistema", "Nuevo usuario creado: " + txtNombreUsuario.getText().trim(), "Admin", "Usuarios");
            limpiarFormUsuario(); cargarTablaUsuarios();
        } else Alertas.error("Error", "No se pudo guardar. Verifique nombre de usuario único.");
    }

    @FXML private void cambiarContrasena() {
        if (txtIdUsuario.getText().isEmpty() || editandoUsuarioId <= 0) {
            Alertas.advertencia("Seleccionar", "Seleccione un usuario de la tabla primero."); return;
        }
        if (txtPasswordUsuario.getText().isEmpty()) {
            Alertas.advertencia("Validación", "Ingrese la nueva contraseña."); return;
        }
        if (!txtPasswordUsuario.getText().equals(txtConfirmarPassword.getText())) {
            Alertas.advertencia("Validación", "Las contraseñas no coinciden."); return;
        }
        if (EmpleadoDAO.cambiarContrasena(editandoUsuarioId, txtPasswordUsuario.getText().trim())) {
            Alertas.exito("Contraseña", "Contraseña cambiada exitosamente.");
            txtPasswordUsuario.clear(); txtConfirmarPassword.clear();
        } else Alertas.error("Error", "No se pudo cambiar la contraseña.");
    }

    @FXML private void limpiarFormUsuario() {
        txtIdUsuario.clear(); txtNombreUsuario.clear(); txtPasswordUsuario.clear(); txtConfirmarPassword.clear();
        txtPermisosUsuario.clear(); cmbEmpleadoUsuario.setValue(null); cmbRolUsuario.setValue(null);
        cmbEstadoUsuario.setValue("Activo"); editandoUsuarioId = -1;
    }

    private void eliminarUsuario(int id) {
        if (EmpleadoDAO.eliminarUsuario(-id)) { Alertas.exito("Eliminado", "Usuario eliminado."); cargarTablaUsuarios(); }
        else Alertas.error("Error", "No se pudo eliminar.");
    }

    @FXML private void filtrarUsuarios() {
        String q = txtBuscarUsuario.getText().toLowerCase();
        if (q.isEmpty()) { cargarTablaUsuarios(); return; }
        List<Object[]> filtrados = EmpleadoDAO.listarUsuarios().stream()
            .filter(r -> r[2].toString().toLowerCase().contains(q) || r[3].toString().toLowerCase().contains(q) || r[4].toString().toLowerCase().contains(q))
            .collect(Collectors.toList());
        usuariosData.setAll(filtrados);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HISTORIAL
    // ═══════════════════════════════════════════════════════════════

    private void cargarTablaHistorial() { historialData.setAll(EmpleadoDAO.listarHistorial()); }

    private void actualizarMetricasHistorial() {
        lblTotalEventos.setText(String.valueOf(historialData.size()));
        long cambiosCargo = historialData.stream().filter(r -> "Cambio de cargo".equals(r[5])).count();
        long asistencias = historialData.stream().filter(r -> "Asistencia".equals(r[5]) || "Ausencia".equals(r[5])).count();
        long incidencias = historialData.stream().filter(r -> "Suspensión".equals(r[5]) || "Inactivación".equals(r[5]) || "Permiso".equals(r[5])).count();
        lblCambiosCargo.setText(String.valueOf(cambiosCargo));
        lblHistorialAsistencias.setText(String.valueOf(asistencias));
        lblIncidencias.setText(String.valueOf(incidencias));
    }

    @FXML private void filtrarHistorial() {
        LocalDate desde = dateHistorialDesde.getValue();
        LocalDate hasta = dateHistorialHasta.getValue();
        String tipo = cmbFiltroTipoEvento.getValue();
        String q = txtBuscarHistorial.getText().toLowerCase();
        List<Object[]> filtrados = EmpleadoDAO.listarHistorial().stream().filter(r -> {
            boolean matchFecha = true;
            if (desde != null && r[1] instanceof LocalDate) matchFecha = matchFecha && !((LocalDate)r[1]).isBefore(desde);
            if (hasta != null && r[1] instanceof LocalDate) matchFecha = matchFecha && !((LocalDate)r[1]).isAfter(hasta);
            boolean matchTipo = "Todos".equals(tipo) || r[5].equals(tipo);
            boolean matchQ = q.isEmpty() || r[4].toString().toLowerCase().contains(q) || r[5].toString().toLowerCase().contains(q) || r[6].toString().toLowerCase().contains(q);
            return matchFecha && matchTipo && matchQ;
        }).collect(Collectors.toList());
        historialData.setAll(filtrados);
        actualizarMetricasHistorial();
    }

    @FXML private void limpiarFiltrosHistorial() {
        dateHistorialDesde.setValue(null); dateHistorialHasta.setValue(null);
        cmbFiltroTipoEvento.setValue("Todos"); txtBuscarHistorial.clear();
        cargarTablaHistorial(); actualizarMetricasHistorial();
    }

    @FXML private void actualizarHistorial() { cargarTablaHistorial(); actualizarMetricasHistorial(); }
    @FXML private void exportarHistorial() { Alertas.informacion("Exportar", "Próximamente."); }
}
