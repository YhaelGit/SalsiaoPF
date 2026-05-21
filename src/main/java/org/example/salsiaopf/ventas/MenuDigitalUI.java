package org.example.salsiaopf.ventas;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Construye componentes visuales del menú digital (tarjetas de producto).
 */
public final class MenuDigitalUI {

    private MenuDigitalUI() {
    }

    public static VBox crearTarjetaProducto(ProductoMenu producto, Consumer<ProductoMenu> alAgregar) {
        VBox card = new VBox(10);
        card.getStyleClass().add("ventasProductCard");
        card.setPrefWidth(220);
        card.setMinWidth(200);
        card.setMaxWidth(240);

        StackPane imagenWrap = new StackPane();
        imagenWrap.getStyleClass().add("ventasProductImageWrap");
        Label emoji = new Label(producto.getEmoji());
        emoji.getStyleClass().add("ventasProductEmoji");
        imagenWrap.getChildren().add(emoji);

        Label nombre = new Label(producto.getNombre());
        nombre.getStyleClass().add("ventasProductName");
        nombre.setWrapText(true);

        Label desc = new Label(producto.getDescripcion());
        desc.getStyleClass().add("ventasProductDesc");
        desc.setWrapText(true);
        desc.setMaxWidth(200);

        HBox precioRow = new HBox(8);
        precioRow.setAlignment(Pos.CENTER_LEFT);
        Label precio = new Label(producto.getPrecioFormateado());
        precio.getStyleClass().add("ventasProductPrice");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        precioRow.getChildren().addAll(precio, spacer);

        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.getStyleClass().add("ventasBtnAgregar");
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> {
            animarAgregar(card);
            alAgregar.accept(producto);
        });

        card.getChildren().addAll(imagenWrap, nombre, desc, precioRow, btnAgregar);
        return card;
    }

    public static VBox crearFilaCarrito(ItemCarrito item,
                                       Runnable onMas,
                                       Runnable onMenos,
                                       Runnable onEliminar) {
        VBox fila = new VBox(8);
        fila.getStyleClass().add("ventasCarritoItem");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label emoji = new Label(item.getProducto().getEmoji());
        emoji.getStyleClass().add("ventasCarritoItemEmoji");

        VBox info = new VBox(2);
        Label nombre = new Label(item.getProducto().getNombre());
        nombre.getStyleClass().add("ventasCarritoItemName");
        Label precioUnit = new Label(item.getProducto().getPrecioFormateado() + " c/u");
        precioUnit.getStyleClass().add("ventasCarritoItemUnit");
        info.getChildren().addAll(nombre, precioUnit);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button btnEliminar = new Button("✕");
        btnEliminar.getStyleClass().add("ventasBtnEliminarItem");
        btnEliminar.setOnAction(e -> onEliminar.run());

        header.getChildren().addAll(emoji, info, btnEliminar);

        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER_LEFT);

        Button btnMenos = new Button("−");
        btnMenos.getStyleClass().add("ventasBtnQty");
        btnMenos.setOnAction(e -> onMenos.run());

        Label lblQty = new Label(String.valueOf(item.getCantidad()));
        lblQty.getStyleClass().add("ventasQtyLabel");
        lblQty.setMinWidth(28);
        lblQty.setAlignment(Pos.CENTER);

        Button btnMas = new Button("+");
        btnMas.getStyleClass().add("ventasBtnQty");
        btnMas.setOnAction(e -> onMas.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label subtotal = new Label(String.format("RD$ %,.0f", item.getSubtotal()));
        subtotal.getStyleClass().add("ventasCarritoItemSubtotal");

        controles.getChildren().addAll(btnMenos, lblQty, btnMas, spacer, subtotal);

        fila.getChildren().addAll(header, controles);
        return fila;
    }

    private static void animarAgregar(VBox card) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.04);
        st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }
}
