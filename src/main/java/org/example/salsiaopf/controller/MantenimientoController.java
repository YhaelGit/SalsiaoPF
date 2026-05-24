package org.example.salsiaopf.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.salsiaopf.dao.MantenimientoDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MantenimientoController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual;
    @FXML private Button btnNotificaciones, btnDashboard, btnEquipos, btnReportar, btnSolicitudes, btnRealizados, btnTecnicos, btnCalendario, btnCostos;
    @FXML private StackPane contenedorMantenimiento;
    @FXML private ScrollPane scrollDashboard, scrollEquipos, scrollReportar, scrollSolicitudes, scrollRealizados, scrollTecnicos, scrollCalendario, scrollCostos;

    // Dashboard
    @FXML private Label lblEquiposBuenos, lblEquiposFallas, lblPendientes, lblEnProceso, lblFinalizados, lblCostosMes;
    @FXML private TextField txtBuscarDashboard;
    @FXML private ComboBox<String> cmbFiltroEstadoD, cmbFiltroPrioridadD;
    @FXML private TableView<Object[]> tablaDashboard;

    // Equipos
    @FXML private TextField txtCodigoEquipo, txtNombreEquipo, txtMarcaEquipo, txtModeloEquipo, txtObsEquipo, txtBuscarEquipo;
    @FXML private ComboBox<String> cmbAreaEquipo, cmbEstadoEquipo, cmbFiltroEstadoEquipo;
    @FXML private DatePicker dateCompraEquipo;
    @FXML private TableView<Object[]> tablaEquipos;

    // Reportar Avería
    @FXML private ComboBox<String> cmbEquipoReporte, cmbPrioridadReporte;
    @FXML private TextField txtAreaReporte, txtPersonaReporte;
    @FXML private TextArea txtDescReporte;
    @FXML private DatePicker dateReporte;

    // Solicitudes
    @FXML private TextField txtBuscarSolicitud;
    @FXML private ComboBox<String> cmbFiltroEstadoSol, cmbFiltroPrioridadSol;
    @FXML private TableView<Object[]> tablaSolicitudes;

    // Realizados
    @FXML private TextField txtBuscarRealizado;
    @FXML private DatePicker dateDesdeReal, dateHastaReal;
    @FXML private TableView<Object[]> tablaRealizados;

    // Técnicos
    @FXML private TextField txtNombreTec, txtTelefonoTec, txtEmpresaTec, txtObsTec, txtBuscarTec;
    @FXML private ComboBox<String> cmbEspecialidadTec, cmbTipoTec, cmbEstadoTec;
    @FXML private TableView<Object[]> tablaTecnicos;

    // Calendario
    @FXML private ComboBox<String> cmbEquipoCal, cmbTipoCal, cmbResponsableCal, cmbFiltroEstadoCal;
    @FXML private DatePicker dateCal;
    @FXML private TextField txtObsCal, txtBuscarCal;
    @FXML private TableView<Object[]> tablaCalendario;

    // Costos
    @FXML private Label lblCostoTotalMes, lblPromedioCosto;
    @FXML private TextField txtBuscarCosto;
    @FXML private DatePicker dateDesdeCosto, dateHastaCosto;
    @FXML private TableView<Object[]> tablaCostos;

    // Data lists
    private ObservableList<Object[]> dashboardData = FXCollections.observableArrayList();
    private ObservableList<Object[]> equiposData = FXCollections.observableArrayList();
    private ObservableList<Object[]> solicitudesData = FXCollections.observableArrayList();
    private ObservableList<Object[]> realizadosData = FXCollections.observableArrayList();
    private ObservableList<Object[]> tecnicosData = FXCollections.observableArrayList();
    private ObservableList<Object[]> calendarioData = FXCollections.observableArrayList();
    private ObservableList<Object[]> costosData = FXCollections.observableArrayList();

    private int editandoEquipoId = -1;
    private int editandoTecnicoId = -1;
    private int editandoCalId = -1;

    // Index constants for listarMantenimientos: 0=ID,1=Codigo,2=Equipo,3=CodEquipo,4=Area,5=Problema,6=Fecha,7=Prioridad,8=Reporta,9=Tecnico,10=Estado,11=Solucion,12=FecSol,13=Tiempo,14=CostoPiezas,15=CostoMO,16=Costo
    private static final int I_MTTO_ID = 0, I_MTTO_COD = 1, I_MTTO_EQ = 2, I_MTTO_AREA = 4, I_MTTO_PROB = 5, I_MTTO_FECHA = 6, I_MTTO_PRIO = 7, I_MTTO_RESP = 8, I_MTTO_TEC = 9, I_MTTO_EST = 10, I_MTTO_SOL = 11, I_MTTO_FECSOL = 12, I_MTTO_TIEM = 13, I_MTTO_CPIEZ = 14, I_MTTO_CMO = 15, I_MTTO_COS = 16;

    @FXML
    private void initialize() {
        ControllerUtil.cargarLogo(logoImage);
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
        MantenimientoDAO.crearTablasSiNoExisten();
        cargarCombos();
        configurarTablaDashboard();
        configurarTablaEquipos();
        configurarTablaSolicitudes();
        configurarTablaRealizados();
        configurarTablaTecnicos();
        configurarTablaCalendario();
        configurarTablaCostos();
        cargarDashboard();
        mostrarDashboard();
    }

    private void cargarCombos() {
        String[] areas = {"Cocina", "Panadería", "Limpieza", "Administración", "Ventas", "Almacén", "Comedor", "Exterior", "Baños", "Cámara fría"};
        if (cmbAreaEquipo != null) cmbAreaEquipo.getItems().setAll(areas);
        String[] estadosEq = {"Bueno", "Regular", "Averiado", "En reparación"};
        if (cmbEstadoEquipo != null) cmbEstadoEquipo.getItems().setAll(estadosEq);
        if (cmbFiltroEstadoEquipo != null) { cmbFiltroEstadoEquipo.getItems().setAll("Todos", "Bueno", "Regular", "Averiado", "En reparación"); cmbFiltroEstadoEquipo.setValue("Todos"); }
        String[] prioridades = {"Alta", "Media", "Baja"};
        if (cmbPrioridadReporte != null) cmbPrioridadReporte.getItems().setAll(prioridades);
        if (cmbFiltroPrioridadD != null) { cmbFiltroPrioridadD.getItems().setAll("Todas", "Alta", "Media", "Baja"); cmbFiltroPrioridadD.setValue("Todas"); }
        if (cmbFiltroPrioridadSol != null) { cmbFiltroPrioridadSol.getItems().setAll("Todas", "Alta", "Media", "Baja"); cmbFiltroPrioridadSol.setValue("Todas"); }
        String[] estadosMtto = {"Todos", "Pendiente", "En proceso", "Resuelto", "Cancelado"};
        if (cmbFiltroEstadoD != null) { cmbFiltroEstadoD.getItems().setAll(estadosMtto); cmbFiltroEstadoD.setValue("Todos"); }
        if (cmbFiltroEstadoSol != null) { cmbFiltroEstadoSol.getItems().setAll(estadosMtto); cmbFiltroEstadoSol.setValue("Todos"); }
        String[] espTec = {"Refrigeración", "Electricidad", "Gas/Plomería", "Mecánica", "Electrónica", "Informática", "General", "Aire Acondicionado", "Carpintería"};
        if (cmbEspecialidadTec != null) cmbEspecialidadTec.getItems().setAll(espTec);
        String[] tipoTec = {"Interno", "Externo"};
        if (cmbTipoTec != null) cmbTipoTec.getItems().setAll(tipoTec);
        String[] estadosTec = {"Activo", "Inactivo"};
        if (cmbEstadoTec != null) cmbEstadoTec.getItems().setAll(estadosTec);
        String[] tiposCal = {"Limpieza general", "Revisión preventiva", "Cambio de piezas", "Mantenimiento eléctrico", "Revisión de gas", "Limpieza de extractor", "Revisión de nevera", "Mantenimiento AA", "Revisión de red"};
        if (cmbTipoCal != null) cmbTipoCal.getItems().setAll(tiposCal);
        String[] estadosCal = {"Todos", "Pendiente", "Realizado", "Cancelado"};
        if (cmbFiltroEstadoCal != null) { cmbFiltroEstadoCal.getItems().setAll(estadosCal); cmbFiltroEstadoCal.setValue("Todos"); }
        cargarCombosDinamicos();
    }

    private void cargarCombosDinamicos() {
        List<String> equipos = MantenimientoDAO.obtenerNombresEquipos();
        String[] eqArr = equipos.toArray(new String[0]);
        if (cmbEquipoReporte != null) cmbEquipoReporte.getItems().setAll(eqArr);
        if (cmbEquipoCal != null) cmbEquipoCal.getItems().setAll(eqArr);
        List<String> tecnicos = MantenimientoDAO.obtenerNombresTecnicos();
        String[] tecArr = tecnicos.toArray(new String[0]);
        if (cmbResponsableCal != null) cmbResponsableCal.getItems().setAll(tecArr);
    }

    // ═══════════════════════════ TABLE CONFIG ═══════════════════════════

    @SuppressWarnings("unchecked")
    private void configurarCols(TableView<Object[]> table, int[] indices) {
        if (table == null) return;
        int n = Math.min(indices.length, table.getColumns().size());
        for (int i = 0; i < n; i++) {
            TableColumn<Object[], Object> col = (TableColumn<Object[], Object>) table.getColumns().get(i);
            final int idx = indices[i];
            col.setCellValueFactory(cd -> {
                Object[] row = cd.getValue();
                if (row == null || row.length <= idx || row[idx] == null) return new SimpleObjectProperty<>("");
                Object v = row[idx];
                if (v instanceof Double) return new SimpleObjectProperty<>(String.format("%.2f", v));
                return new SimpleObjectProperty<>(v.toString());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void configurarAcciones(TableView<T> table, java.util.function.Consumer<Integer> onEdit, java.util.function.Consumer<Integer> onDelete) {
        if (table == null || table.getColumns().isEmpty()) return;
        TableColumn<T, Void> col = (TableColumn<T, Void>) table.getColumns().get(table.getColumns().size() - 1);
        col.setCellFactory(c -> new TableCell<>() {
            private final Button bEdit = new Button("✎"), bDel = new Button("🗑");
            { bEdit.getStyleClass().addAll("cellActionBtn","actionOrange"); bDel.getStyleClass().addAll("cellActionBtn","actionRed"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                int id = (int)((Object[])getTableView().getItems().get(getIndex()))[0];
                bEdit.setOnAction(e -> { if (onEdit != null) onEdit.accept(id); });
                bDel.setOnAction(e -> { if (onDelete != null) onDelete.accept(id); });
                setGraphic(new HBox(4, bEdit, bDel));
            }
        });
    }

    private void configurarTablaDashboard() {
        tablaDashboard.setItems(dashboardData);
        configurarCols(tablaDashboard, new int[]{I_MTTO_COD, I_MTTO_EQ, I_MTTO_AREA, I_MTTO_PROB, I_MTTO_EST, I_MTTO_PRIO, I_MTTO_FECHA, I_MTTO_TEC});
        configurarAcciones(tablaDashboard, id -> mostrarSolicitudes(), id -> eliminarSolicitud(id));
    }

    private void configurarTablaEquipos() {
        tablaEquipos.setItems(equiposData);
        configurarCols(tablaEquipos, new int[]{0,1,2,3,4,5,6,7});
        configurarAcciones(tablaEquipos, id -> cargarEquipoEdicion(id), id -> eliminarEquipoPorId(id));
    }

    private void configurarTablaSolicitudes() {
        tablaSolicitudes.setItems(solicitudesData);
        configurarCols(tablaSolicitudes, new int[]{I_MTTO_COD, I_MTTO_EQ, I_MTTO_PROB, I_MTTO_PRIO, I_MTTO_TEC, I_MTTO_FECHA, I_MTTO_EST});
        if (!tablaSolicitudes.getColumns().isEmpty()) {
            int n = tablaSolicitudes.getColumns().size();
            TableColumn<Object[], Void> colAcc = (TableColumn<Object[], Void>) tablaSolicitudes.getColumns().get(n-1);
            colAcc.setCellFactory(c -> new TableCell<>() {
                Button bVer = new Button("👁 Ver"), bAsig = new Button("👤 Asignar"), bFin = new Button("✅ Fin");
                { bVer.getStyleClass().addAll("cellActionBtn","actionBlue"); bAsig.getStyleClass().addAll("cellActionBtn","actionOrange"); bFin.getStyleClass().addAll("cellActionBtn","actionGreen"); }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setGraphic(null); return; }
                    Object[] r = getTableView().getItems().get(getIndex());
                    int id = (int)r[I_MTTO_ID]; String est = (String)r[I_MTTO_EST];
                    bVer.setOnAction(e -> verDetalleSolicitud(id));
                    bAsig.setOnAction(e -> asignarTecnicoSolicitud(id));
                    bFin.setOnAction(e -> finalizarSolicitud(id));
                    bAsig.setDisable(!"Pendiente".equals(est));
                    bFin.setDisable(!"En proceso".equals(est));
                    setGraphic(new HBox(4, bVer, bAsig, bFin));
                }
            });
        }
    }

    private void configurarTablaRealizados() {
        tablaRealizados.setItems(realizadosData);
        configurarCols(tablaRealizados, new int[]{I_MTTO_COD, I_MTTO_EQ, I_MTTO_PROB, I_MTTO_SOL, I_MTTO_TEC, I_MTTO_FECSOL, I_MTTO_COS, I_MTTO_TIEM});
        configurarAcciones(tablaRealizados, id -> verDetalleSolicitud(id), id -> {});
    }

    private void configurarTablaTecnicos() {
        tablaTecnicos.setItems(tecnicosData);
        configurarCols(tablaTecnicos, new int[]{0,1,2,3,4,5,6});
        configurarAcciones(tablaTecnicos, id -> cargarTecnicoEdicion(id), id -> eliminarTecnicoPorId(id));
    }

    private void configurarTablaCalendario() {
        tablaCalendario.setItems(calendarioData);
        configurarCols(tablaCalendario, new int[]{0,1,2,3,4,5});
        configurarAcciones(tablaCalendario, id -> cargarCalendarioEdicion(id), id -> eliminarCalendarioPorId(id));
    }

    private void configurarTablaCostos() {
        tablaCostos.setItems(costosData);
        configurarCols(tablaCostos, new int[]{I_MTTO_COD, I_MTTO_EQ, I_MTTO_PROB, I_MTTO_CPIEZ, I_MTTO_CMO, I_MTTO_COS, I_MTTO_FECSOL, I_MTTO_TEC});
    }

    // ═══════════════════════════ NAVEGACIÓN ═══════════════════════════

    private void ocultarTodos() {
        for (ScrollPane sp : new ScrollPane[]{scrollDashboard, scrollEquipos, scrollReportar, scrollSolicitudes, scrollRealizados, scrollTecnicos, scrollCalendario, scrollCostos}) {
            if (sp != null) { sp.setVisible(false); sp.setManaged(false); }
        }
        for (Button btn : new Button[]{btnDashboard, btnEquipos, btnReportar, btnSolicitudes, btnRealizados, btnTecnicos, btnCalendario, btnCostos}) {
            if (btn != null) btn.getStyleClass().remove("sideButtonActive");
        }
    }

    private void mostrarScroll(ScrollPane sp, Button btn) {
        ocultarTodos();
        if (sp != null) { sp.setVisible(true); sp.setManaged(true); }
        if (btn != null) btn.getStyleClass().add("sideButtonActive");
    }

    @FXML private void mostrarDashboard() { mostrarScroll(scrollDashboard, btnDashboard); cargarDashboard(); }
    @FXML private void mostrarEquipos() { mostrarScroll(scrollEquipos, btnEquipos); cargarEquipos(); }
    @FXML private void mostrarReportar() { mostrarScroll(scrollReportar, btnReportar); cargarCombosDinamicos(); }
    @FXML private void mostrarSolicitudes() { mostrarScroll(scrollSolicitudes, btnSolicitudes); cargarSolicitudes(); }
    @FXML private void mostrarRealizados() { mostrarScroll(scrollRealizados, btnRealizados); cargarRealizados(); }
    @FXML private void mostrarTecnicos() { mostrarScroll(scrollTecnicos, btnTecnicos); cargarTecnicos(); }
    @FXML private void mostrarCalendario() { mostrarScroll(scrollCalendario, btnCalendario); cargarCalendario(); cargarCombosDinamicos(); }
    @FXML private void mostrarCostos() { mostrarScroll(scrollCostos, btnCostos); cargarCostos(); }
    @FXML private void volverMenu(ActionEvent e) { Navegacion.volverCentroSistema(e); }
    @FXML private void salirSistema() { System.exit(0); }
    @FXML private void mostrarNotificaciones() { Alertas.informacion("Notificaciones", "No hay notificaciones pendientes."); }

    // ═══════════════════════════ DASHBOARD ═══════════════════════════

    private void cargarDashboard() {
        try {
            lblEquiposBuenos.setText(String.valueOf(MantenimientoDAO.equiposBuenEstado()));
            lblEquiposFallas.setText(String.valueOf(MantenimientoDAO.equiposConFallas()));
            lblPendientes.setText(String.valueOf(MantenimientoDAO.mantenimientosPendientes()));
            lblEnProceso.setText(String.valueOf(MantenimientoDAO.mantenimientosEnProceso()));
            lblFinalizados.setText(String.valueOf(MantenimientoDAO.mantenimientosFinalizados()));
            lblCostosMes.setText("RD$ " + String.format("%.2f", MantenimientoDAO.totalCostosMes()));
            cargarDashboardTabla();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarDashboardTabla() {
        List<Object[]> lista = MantenimientoDAO.listarMantenimientos();
        dashboardData.setAll(lista);
        aplicarFiltrosDashboard();
    }

    private void aplicarFiltrosDashboard() {
        String estF = cmbFiltroEstadoD.getValue();
        String prioF = cmbFiltroPrioridadD.getValue();
        String busq = txtBuscarDashboard.getText().toLowerCase().trim();
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            if (estF != null && !"Todos".equals(estF) && !estF.equals(r[I_MTTO_EST])) continue;
            if (prioF != null && !"Todas".equals(prioF) && !prioF.equals(r[I_MTTO_PRIO])) continue;
            if (!busq.isEmpty()) {
                boolean match = false;
                for (int i = 0; i < r.length; i++) { if (r[i] != null && r[i].toString().toLowerCase().contains(busq)) { match = true; break; } }
                if (!match) continue;
            }
            filt.add(r);
        }
        dashboardData.setAll(filt);
    }

    @FXML private void buscarDashboard() { aplicarFiltrosDashboard(); }
    @FXML private void limpiarDashboard() {
        txtBuscarDashboard.clear();
        cmbFiltroEstadoD.setValue("Todos");
        cmbFiltroPrioridadD.setValue("Todas");
        cargarDashboardTabla();
    }
    @FXML private void exportarMantenimientos() { Alertas.informacion("Exportar", "Próximamente: exportación a Excel/PDF."); }

    // ═══════════════════════════ EQUIPOS ═══════════════════════════

    private void cargarEquipos() {
        List<Object[]> lista = MantenimientoDAO.listarEquipos();
        equiposData.setAll(lista);
    }

    @FXML private void nuevoEquipo() {
        editandoEquipoId = -1;
        limpiarEquipo();
        txtCodigoEquipo.setText(MantenimientoDAO.obtenerCodigoEquipo());
    }

    @FXML private void guardarEquipo() {
        String cod = txtCodigoEquipo.getText().trim();
        String nom = txtNombreEquipo.getText().trim();
        if (cod.isEmpty() || nom.isEmpty()) { Alertas.advertencia("Validación", "Código y Nombre son obligatorios."); return; }
        String area = cmbAreaEquipo.getValue() != null ? cmbAreaEquipo.getValue() : "";
        String marca = txtMarcaEquipo.getText().trim();
        String modelo = txtModeloEquipo.getText().trim();
        String fecha = dateCompraEquipo.getValue() != null ? dateCompraEquipo.getValue().toString() : "";
        String estado = cmbEstadoEquipo.getValue() != null ? cmbEstadoEquipo.getValue() : "Bueno";
        String obs = txtObsEquipo.getText().trim();
        boolean ok;
        if (editandoEquipoId > 0) ok = MantenimientoDAO.actualizarEquipo(editandoEquipoId, cod, nom, area, marca, modelo, fecha, estado, obs);
        else ok = MantenimientoDAO.guardarEquipo(cod, nom, area, marca, modelo, fecha, estado, obs) > 0;
        if (ok) { Alertas.exito("Equipo", editandoEquipoId > 0 ? "Equipo actualizado." : "Equipo guardado."); limpiarEquipo(); cargarEquipos(); cargarCombosDinamicos(); }
        else Alertas.error("Error", "No se pudo guardar el equipo.");
    }

    private void cargarEquipoEdicion(int id) {
        for (Object[] r : equiposData) {
            if ((int)r[0] == id) {
                editandoEquipoId = id;
                txtCodigoEquipo.setText((String)r[1]);
                txtNombreEquipo.setText((String)r[2]);
                cmbAreaEquipo.setValue((String)r[3]);
                txtMarcaEquipo.setText((String)r[4]);
                txtModeloEquipo.setText((String)r[5]);
                String f = (String)r[6];
                if (f != null && !f.isEmpty()) try { dateCompraEquipo.setValue(LocalDate.parse(f)); } catch (Exception ex) { dateCompraEquipo.setValue(null); }
                cmbEstadoEquipo.setValue((String)r[7]);
                txtObsEquipo.setText((String)r[8]);
                return;
            }
        }
    }

    @FXML private void editarEquipo() {
        if (editandoEquipoId <= 0) { Alertas.advertencia("Editar", "Primero seleccione un equipo en la tabla."); return; }
        guardarEquipo();
    }

    private void eliminarEquipoPorId(int id) {
        if (Alertas.confirmar("Eliminar", "¿Eliminar este equipo?")) {
            if (MantenimientoDAO.eliminarEquipo(id)) { Alertas.exito("Eliminado", ""); cargarEquipos(); cargarCombosDinamicos(); }
            else Alertas.error("Error", "No se pudo eliminar.");
        }
    }

    @FXML private void eliminarEquipo() { if (editandoEquipoId > 0) eliminarEquipoPorId(editandoEquipoId); }
    @FXML private void limpiarEquipo() {
        editandoEquipoId = -1;
        txtCodigoEquipo.clear(); txtNombreEquipo.clear(); txtMarcaEquipo.clear(); txtModeloEquipo.clear(); txtObsEquipo.clear();
        cmbAreaEquipo.setValue(null); cmbEstadoEquipo.setValue(null); dateCompraEquipo.setValue(null);
    }
    @FXML private void buscarEquipo() {
        String busq = txtBuscarEquipo.getText().toLowerCase().trim();
        String estF = cmbFiltroEstadoEquipo.getValue();
        List<Object[]> all = MantenimientoDAO.listarEquipos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            if (estF != null && !"Todos".equals(estF) && !estF.equals(r[7])) continue;
            if (!busq.isEmpty()) { boolean m = false; for (Object v : r) { if (v != null && v.toString().toLowerCase().contains(busq)) { m = true; break; } } if (!m) continue; }
            filt.add(r);
        }
        equiposData.setAll(filt);
    }
    @FXML private void limpiarFiltrosEquipo() { txtBuscarEquipo.clear(); cmbFiltroEstadoEquipo.setValue("Todos"); cargarEquipos(); }

    // ═══════════════════════════ REPORTAR AVERÍA ═══════════════════════════

    @FXML private void guardarReporte() {
        String sel = cmbEquipoReporte.getValue();
        if (sel == null || sel.isEmpty()) { Alertas.advertencia("Validación", "Seleccione un equipo."); return; }
        int idEquipo = Integer.parseInt(sel.split(" - ")[0]);
        String area = txtAreaReporte.getText().trim();
        String prob = txtDescReporte.getText().trim();
        if (prob.isEmpty()) { Alertas.advertencia("Validación", "Describa el problema."); return; }
        String fecha = dateReporte.getValue() != null ? dateReporte.getValue().toString() : LocalDate.now().toString();
        String prio = cmbPrioridadReporte.getValue() != null ? cmbPrioridadReporte.getValue() : "Media";
        String pers = txtPersonaReporte.getText().trim();
        String cod = MantenimientoDAO.obtenerCodigoMantenimiento();
        int id = MantenimientoDAO.guardarMantenimiento(cod, idEquipo, prob, fecha, prio, pers);
        if (id > 0) { Alertas.exito("Reporte", "Avería reportada correctamente. Código: " + cod); limpiarReporte(); }
        else Alertas.error("Error", "No se pudo guardar el reporte.");
    }

    @FXML private void limpiarReporte() {
        cmbEquipoReporte.setValue(null); txtAreaReporte.clear(); txtDescReporte.clear();
        dateReporte.setValue(LocalDate.now()); cmbPrioridadReporte.setValue("Media"); txtPersonaReporte.clear();
    }

    // ═══════════════════════════ SOLICITUDES ═══════════════════════════

    private void cargarSolicitudes() {
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            String est = (String)r[I_MTTO_EST];
            if ("Pendiente".equals(est) || "En proceso".equals(est)) filt.add(r);
        }
        solicitudesData.setAll(filt);
    }

    private void verDetalleSolicitud(int id) {
        for (Object[] r : MantenimientoDAO.listarMantenimientos()) {
            if ((int)r[I_MTTO_ID] == id) {
                String msg = "Código: " + r[I_MTTO_COD] + "\nEquipo: " + r[I_MTTO_EQ] + "\nProblema: " + r[I_MTTO_PROB] + "\nPrioridad: " + r[I_MTTO_PRIO] + "\nReporta: " + r[I_MTTO_RESP] + "\nFecha: " + r[I_MTTO_FECHA] + "\nEstado: " + r[I_MTTO_EST] + "\nTécnico: " + r[I_MTTO_TEC] + "\nSolución: " + r[I_MTTO_SOL] + "\nFecha Sol: " + r[I_MTTO_FECSOL] + "\nCosto: RD$ " + String.format("%.2f", r[I_MTTO_COS]);
                Alertas.informacion("Detalle #" + id, msg);
                return;
            }
        }
    }

    private void asignarTecnicoSolicitud(int id) {
        List<String> tecList = MantenimientoDAO.obtenerNombresTecnicos();
        if (tecList.isEmpty()) { Alertas.advertencia("Asignar", "No hay técnicos registrados. Cree uno en la sección Técnicos."); return; }
        ChoiceDialog<String> d = new ChoiceDialog<>(tecList.get(0), tecList);
        d.setTitle("Asignar Técnico");
        d.setHeaderText("Seleccione el técnico para esta solicitud");
        d.setContentText("Técnico:");
        Optional<String> r = d.showAndWait();
        if (r.isPresent()) {
            int idTec = Integer.parseInt(r.get().split(" - ")[0]);
            if (MantenimientoDAO.asignarTecnico(id, idTec)) { Alertas.exito("Asignado", "Técnico asignado correctamente."); cargarSolicitudes(); }
            else Alertas.error("Error", "No se pudo asignar.");
        }
    }

    private void finalizarSolicitud(int id) {
        TextInputDialog dSol = new TextInputDialog();
        dSol.setTitle("Finalizar Mantenimiento");
        dSol.setHeaderText("Complete los datos para finalizar");
        dSol.setContentText("Solución aplicada:");
        Optional<String> rSol = dSol.showAndWait();
        if (!rSol.isPresent() || rSol.get().trim().isEmpty()) return;
        TextInputDialog dCost = new TextInputDialog("0");
        dCost.setContentText("Costo de piezas (RD$):");
        Optional<String> rCost = dCost.showAndWait();
        if (!rCost.isPresent()) return;
        double cPiezas = 0; try { cPiezas = Double.parseDouble(rCost.get()); } catch (Exception e) { cPiezas = 0; }
        TextInputDialog dMO = new TextInputDialog("0");
        dMO.setContentText("Costo de mano de obra (RD$):");
        Optional<String> rMO = dMO.showAndWait();
        if (!rMO.isPresent()) return;
        double cMO = 0; try { cMO = Double.parseDouble(rMO.get()); } catch (Exception e) { cMO = 0; }
        TextInputDialog dTiempo = new TextInputDialog("");
        dTiempo.setContentText("Tiempo de reparación (ej: 2 horas):");
        Optional<String> rTiempo = dTiempo.showAndWait();
        if (!rTiempo.isPresent()) return;
        if (MantenimientoDAO.finalizarMantenimiento(id, rSol.get(), LocalDate.now().toString(), rTiempo.get(), cPiezas, cMO, "")) {
            Alertas.exito("Finalizado", "Mantenimiento completado.");
            cargarSolicitudes(); cargarDashboard();
        } else Alertas.error("Error", "No se pudo finalizar.");
    }

    private void eliminarSolicitud(int id) {
        if (Alertas.confirmar("Eliminar", "¿Eliminar esta solicitud?")) {
            if (MantenimientoDAO.eliminarMantenimiento(id)) { Alertas.exito("Eliminado", ""); cargarSolicitudes(); cargarDashboard(); }
            else Alertas.error("Error", "No se pudo eliminar.");
        }
    }

    @FXML private void buscarSolicitud() {
        String busq = txtBuscarSolicitud.getText().toLowerCase().trim();
        String estF = cmbFiltroEstadoSol.getValue();
        String prioF = cmbFiltroPrioridadSol.getValue();
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            String est = (String)r[I_MTTO_EST];
            if (!"Pendiente".equals(est) && !"En proceso".equals(est)) continue;
            if (estF != null && !"Todos".equals(estF) && !estF.equals(est)) continue;
            if (prioF != null && !"Todas".equals(prioF) && !prioF.equals(r[I_MTTO_PRIO])) continue;
            if (!busq.isEmpty()) { boolean m = false; for (Object v : r) { if (v != null && v.toString().toLowerCase().contains(busq)) { m = true; break; } } if (!m) continue; }
            filt.add(r);
        }
        solicitudesData.setAll(filt);
    }
    @FXML private void limpiarSolicitudes() { txtBuscarSolicitud.clear(); cmbFiltroEstadoSol.setValue("Todos"); cmbFiltroPrioridadSol.setValue("Todas"); cargarSolicitudes(); }
    @FXML private void exportarSolicitudes() { Alertas.informacion("Exportar", "Próximamente."); }

    // ═══════════════════════════ REALIZADOS ═══════════════════════════

    private void cargarRealizados() {
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) if ("Resuelto".equals(r[I_MTTO_EST])) filt.add(r);
        realizadosData.setAll(filt);
    }

    @FXML private void buscarRealizado() {
        String busq = txtBuscarRealizado.getText().toLowerCase().trim();
        LocalDate desde = dateDesdeReal.getValue();
        LocalDate hasta = dateHastaReal.getValue();
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            if (!"Resuelto".equals(r[I_MTTO_EST])) continue;
            if (desde != null) { try { LocalDate fd = LocalDate.parse((String)r[I_MTTO_FECSOL]); if (fd.isBefore(desde)) continue; } catch (Exception e) {} }
            if (hasta != null) { try { LocalDate fd = LocalDate.parse((String)r[I_MTTO_FECSOL]); if (fd.isAfter(hasta)) continue; } catch (Exception e) {} }
            if (!busq.isEmpty()) { boolean m = false; for (Object v : r) { if (v != null && v.toString().toLowerCase().contains(busq)) { m = true; break; } } if (!m) continue; }
            filt.add(r);
        }
        realizadosData.setAll(filt);
    }
    @FXML private void limpiarRealizados() { txtBuscarRealizado.clear(); dateDesdeReal.setValue(null); dateHastaReal.setValue(null); cargarRealizados(); }
    @FXML private void exportarRealizados() { Alertas.informacion("Exportar", "Próximamente."); }

    // ═══════════════════════════ TÉCNICOS ═══════════════════════════

    private void cargarTecnicos() {
        List<Object[]> lista = MantenimientoDAO.listarTecnicos();
        tecnicosData.setAll(lista);
    }

    @FXML private void nuevoTecnico() { editandoTecnicoId = -1; limpiarTecnico(); }
    @FXML private void guardarTecnico() {
        String nom = txtNombreTec.getText().trim();
        if (nom.isEmpty()) { Alertas.advertencia("Validación", "El nombre es obligatorio."); return; }
        String tel = txtTelefonoTec.getText().trim();
        String esp = cmbEspecialidadTec.getValue() != null ? cmbEspecialidadTec.getValue() : "";
        String tipo = cmbTipoTec.getValue() != null ? cmbTipoTec.getValue() : "Interno";
        String emp = txtEmpresaTec.getText().trim();
        String est = cmbEstadoTec.getValue() != null ? cmbEstadoTec.getValue() : "Activo";
        String obs = txtObsTec.getText().trim();
        boolean ok;
        if (editandoTecnicoId > 0) ok = MantenimientoDAO.actualizarTecnico(editandoTecnicoId, nom, tel, esp, tipo, emp, est, obs);
        else ok = MantenimientoDAO.guardarTecnico(nom, tel, esp, tipo, emp, est, obs) > 0;
        if (ok) { Alertas.exito("Técnico", editandoTecnicoId > 0 ? "Actualizado." : "Guardado."); limpiarTecnico(); cargarTecnicos(); cargarCombosDinamicos(); }
        else Alertas.error("Error", "No se pudo guardar.");
    }

    private void cargarTecnicoEdicion(int id) {
        for (Object[] r : tecnicosData) {
            if ((int)r[0] == id) {
                editandoTecnicoId = id;
                txtNombreTec.setText((String)r[1]); txtTelefonoTec.setText((String)r[2]);
                cmbEspecialidadTec.setValue((String)r[3]); cmbTipoTec.setValue((String)r[4]);
                txtEmpresaTec.setText((String)r[5]); cmbEstadoTec.setValue((String)r[6]);
                return;
            }
        }
    }

    private void eliminarTecnicoPorId(int id) {
        if (Alertas.confirmar("Eliminar", "¿Eliminar este técnico?")) {
            if (MantenimientoDAO.eliminarTecnico(id)) { Alertas.exito("Eliminado", ""); cargarTecnicos(); cargarCombosDinamicos(); }
            else Alertas.error("Error", "No se pudo eliminar.");
        }
    }
    @FXML private void eliminarTecnico() { if (editandoTecnicoId > 0) eliminarTecnicoPorId(editandoTecnicoId); }
    @FXML private void editarTecnico() { if (editandoTecnicoId > 0) guardarTecnico(); }
    @FXML private void limpiarTecnico() {
        editandoTecnicoId = -1;
        txtNombreTec.clear(); txtTelefonoTec.clear(); txtEmpresaTec.clear(); txtObsTec.clear();
        cmbEspecialidadTec.setValue(null); cmbTipoTec.setValue(null); cmbEstadoTec.setValue("Activo");
    }
    @FXML private void buscarTecnico() {
        String busq = txtBuscarTec.getText().toLowerCase().trim();
        List<Object[]> all = MantenimientoDAO.listarTecnicos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            if (!busq.isEmpty()) { boolean m = false; for (Object v : r) { if (v != null && v.toString().toLowerCase().contains(busq)) { m = true; break; } } if (!m) continue; }
            filt.add(r);
        }
        tecnicosData.setAll(filt);
    }
    @FXML private void limpiarFiltrosTec() { txtBuscarTec.clear(); cargarTecnicos(); }

    // ═══════════════════════════ CALENDARIO ═══════════════════════════

    private void cargarCalendario() {
        List<Object[]> lista = MantenimientoDAO.listarCalendario();
        calendarioData.setAll(lista);
    }

    @FXML private void guardarCalendario() {
        String eqSel = cmbEquipoCal.getValue();
        if (eqSel == null || dateCal.getValue() == null) { Alertas.advertencia("Validación", "Seleccione equipo y fecha."); return; }
        int idEq = Integer.parseInt(eqSel.split(" - ")[0]);
        String fecha = dateCal.getValue().toString();
        String tipo = cmbTipoCal.getValue() != null ? cmbTipoCal.getValue() : "";
        String respSel = cmbResponsableCal.getValue();
        int idTec = respSel != null ? Integer.parseInt(respSel.split(" - ")[0]) : 1;
        String obs = txtObsCal.getText().trim();
        boolean ok;
        if (editandoCalId > 0) ok = MantenimientoDAO.actualizarCalendario(editandoCalId, idEq, fecha, tipo, idTec, "Pendiente", obs);
        else ok = MantenimientoDAO.guardarCalendario(idEq, fecha, tipo, idTec, obs) > 0;
        if (ok) { Alertas.exito("Calendario", editandoCalId > 0 ? "Actualizado." : "Programado."); limpiarCalendario(); cargarCalendario(); }
        else Alertas.error("Error", "No se pudo guardar.");
    }

    private void cargarCalendarioEdicion(int id) {
        for (Object[] r : calendarioData) {
            if ((int)r[0] == id) {
                editandoCalId = id;
                String eq = (String)r[1];
                for (String e : cmbEquipoCal.getItems()) { if (e.contains(eq)) { cmbEquipoCal.setValue(e); break; } }
                try { dateCal.setValue(LocalDate.parse((String)r[2])); } catch (Exception ex) { dateCal.setValue(null); }
                cmbTipoCal.setValue((String)r[3]);
                String resp = (String)r[4];
                for (String tec : cmbResponsableCal.getItems()) { if (tec.contains(resp)) { cmbResponsableCal.setValue(tec); break; } }
                txtObsCal.setText((String)r[6]);
                return;
            }
        }
    }

    private void eliminarCalendarioPorId(int id) {
        if (Alertas.confirmar("Eliminar", "¿Eliminar esta programación?")) {
            if (MantenimientoDAO.eliminarCalendario(id)) { Alertas.exito("Eliminado", ""); cargarCalendario(); }
            else Alertas.error("Error", "No se pudo eliminar.");
        }
    }
    @FXML private void eliminarCalendario() { if (editandoCalId > 0) eliminarCalendarioPorId(editandoCalId); }
    @FXML private void editarCalendario() { if (editandoCalId > 0) guardarCalendario(); }
    @FXML private void marcarRealizadoCal() {
        if (editandoCalId <= 0) { Alertas.advertencia("Marcar", "Seleccione un registro de la tabla."); return; }
        if (MantenimientoDAO.marcarCalendarioRealizado(editandoCalId)) { Alertas.exito("Realizado", "Marcado como completado."); limpiarCalendario(); cargarCalendario(); }
        else Alertas.error("Error", "No se pudo marcar.");
    }
    @FXML private void limpiarCalendario() {
        editandoCalId = -1; cmbEquipoCal.setValue(null); dateCal.setValue(null);
        cmbTipoCal.setValue(null); cmbResponsableCal.setValue(null); txtObsCal.clear();
    }
    @FXML private void buscarCalendario() {
        String busq = txtBuscarCal.getText().toLowerCase().trim();
        String estF = cmbFiltroEstadoCal.getValue();
        List<Object[]> all = MantenimientoDAO.listarCalendario();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            if (estF != null && !"Todos".equals(estF) && !estF.equals(r[5])) continue;
            if (!busq.isEmpty()) { boolean m = false; for (Object v : r) { if (v != null && v.toString().toLowerCase().contains(busq)) { m = true; break; } } if (!m) continue; }
            filt.add(r);
        }
        calendarioData.setAll(filt);
    }
    @FXML private void limpiarFiltrosCal() { txtBuscarCal.clear(); cmbFiltroEstadoCal.setValue("Todos"); cargarCalendario(); }

    // ═══════════════════════════ COSTOS ═══════════════════════════

    private void cargarCostos() {
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        double total = 0; int count = 0;
        for (Object[] r : all) {
            if ("Resuelto".equals(r[I_MTTO_EST]) || "Pendiente".equals(r[I_MTTO_EST]) || "En proceso".equals(r[I_MTTO_EST])) {
                filt.add(r);
                total += (Double)r[I_MTTO_COS];
                count++;
            }
        }
        costosData.setAll(filt);
        lblCostoTotalMes.setText("RD$ " + String.format("%.2f", MantenimientoDAO.totalCostosMes()));
        lblPromedioCosto.setText("RD$ " + (count > 0 ? String.format("%.2f", total/count) : "0.00"));
    }

    @FXML private void buscarCosto() {
        String busq = txtBuscarCosto.getText().toLowerCase().trim();
        LocalDate desde = dateDesdeCosto.getValue();
        LocalDate hasta = dateHastaCosto.getValue();
        List<Object[]> all = MantenimientoDAO.listarMantenimientos();
        ObservableList<Object[]> filt = FXCollections.observableArrayList();
        for (Object[] r : all) {
            if (!"Resuelto".equals(r[I_MTTO_EST]) && !"Pendiente".equals(r[I_MTTO_EST]) && !"En proceso".equals(r[I_MTTO_EST])) continue;
            if (desde != null) { try { LocalDate fd = LocalDate.parse((String)r[I_MTTO_FECHA]); if (fd.isBefore(desde)) continue; } catch (Exception e) {} }
            if (hasta != null) { try { LocalDate fd = LocalDate.parse((String)r[I_MTTO_FECHA]); if (fd.isAfter(hasta)) continue; } catch (Exception e) {} }
            if (!busq.isEmpty()) { boolean m = false; for (Object v : r) { if (v != null && v.toString().toLowerCase().contains(busq)) { m = true; break; } } if (!m) continue; }
            filt.add(r);
        }
        costosData.setAll(filt);
    }
    @FXML private void limpiarCostos() { txtBuscarCosto.clear(); dateDesdeCosto.setValue(null); dateHastaCosto.setValue(null); cargarCostos(); }
    @FXML private void exportarCostos() { Alertas.informacion("Exportar", "Próximamente."); }
}
