package org.example.salsiaopf.ventas;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta"),
    TRANSFERENCIA("Transferencia");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static MetodoPago desdeTexto(String texto) {
        if (texto == null) return EFECTIVO;
        String t = texto.trim().toLowerCase();
        if (t.contains("tarjeta") || t.contains("card")) return TARJETA;
        if (t.contains("transfer")) return TRANSFERENCIA;
        return EFECTIVO;
    }
}
