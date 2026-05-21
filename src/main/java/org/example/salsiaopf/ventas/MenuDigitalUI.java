package org.example.salsiaopf.ventas;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Construye componentes visuales del menu digital (tarjetas de producto).
 * Estilo delivery premium tipo Uber Eats / Rappi.
 */
public final class MenuDigitalUI {

    private MenuDigitalUI() {
    }

    public static VBox crearTarjetaProducto(ProductoMenu producto, Consumer<ProductoMenu> alAgregar) {
        VBox card = new VBox(0);
        card.getStyleClass().add("ventasProductCard");
        card.setPrefWidth(220);
        card.setMinWidth(200);
        card.setMaxWidth(240);

        // Imagen / placeholder con gradiente
        StackPane imagenWrap = crearImagenProducto(producto);

        // Info del producto
        VBox infoBox = new VBox(4);
        infoBox.getStyleClass().add("ventasProductInfo");
        infoBox.setPrefWidth(200);

        Label nombre = new Label(producto.getNombre());
        nombre.getStyleClass().add("ventasProductName");
        nombre.setWrapText(true);
        nombre.setMaxWidth(190);

        Label desc = new Label(producto.getDescripcion());
        desc.getStyleClass().add("ventasProductDesc");
        desc.setWrapText(true);
        desc.setMaxWidth(190);

        infoBox.getChildren().addAll(nombre, desc);

        // Precio + boton
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getStyleClass().add("ventasProductBottom");

        Label precio = new Label(producto.getPrecioFormateado());
        precio.getStyleClass().add("ventasProductPrice");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.getStyleClass().add("ventasBtnAgregar");
        btnAgregar.setOnAction(e -> {
            animarAgregar(card);
            alAgregar.accept(producto);
        });

        bottomRow.getChildren().addAll(precio, spacer, btnAgregar);

        card.getChildren().addAll(imagenWrap, infoBox, bottomRow);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0, 212, 255, 0.25), 20, 0.35, 0, 8);");
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setStyle("");
        });

        return card;
    }

    private static StackPane crearImagenProducto(ProductoMenu producto) {
        StackPane wrap = new StackPane();
        wrap.getStyleClass().add("ventasProductImageWrap");
        wrap.setPrefHeight(130);
        wrap.setMinHeight(130);
        wrap.setMaxHeight(130);

        // Gradiente de fondo segun categoria
        String[] colores = CatalogoSalsiao.gradienteCategoria(producto.getCategoria());
        wrap.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s);",
                colores[0], colores[1]
        ));

        // Clip redondeado
        Rectangle clip = new Rectangle(200, 130);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        wrap.setClip(clip);

        String imgPath = ProductImageRegistry.get(producto.getId());
        try {
            java.io.InputStream is = MenuDigitalUI.class.getResourceAsStream(imgPath);
            if (is != null) {
                Image img = new Image(is, 200, 130, true, true);
                is.close();
                if (!img.isError()) {
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(200);
                    iv.setFitHeight(130);
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);
                    wrap.getChildren().add(iv);
                    StackPane overlay = new StackPane();
                    overlay.setStyle("-fx-background-color: rgba(0,0,0,0.15);");
                    overlay.setPrefSize(200, 130);
                    overlay.setClip(clip);
                    wrap.getChildren().add(overlay);
                    return wrap;
                }
            }
        } catch (Exception ignored) {
        }

        // Placeholder: emoji grande con sombra
        Label emoji = new Label(producto.getEmoji());
        emoji.getStyleClass().add("ventasProductEmoji");
        emoji.setStyle("-fx-font-size: 52px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0.4, 0, 3);");
        wrap.getChildren().add(emoji);

        // Sello de categoria pequeno
        Label catLabel = new Label(producto.getCategoria());
        catLabel.getStyleClass().add("ventasCatBadge");
        catLabel.setStyle(
                "-fx-font-size: 9px; -fx-font-weight: 800; -fx-text-fill: rgba(255,255,255,0.9); "
                + "-fx-background-color: rgba(0,0,0,0.35); -fx-background-radius: 6; "
                + "-fx-padding: 3 8; -fx-alignment: center;"
        );
        StackPane.setAlignment(catLabel, Pos.TOP_LEFT);
        wrap.getChildren().add(catLabel);

        return wrap;
    }

    public static VBox crearFilaCarrito(ItemCarrito item,
                                       Runnable onMas,
                                       Runnable onMenos,
                                       Runnable onEliminar) {
        VBox fila = new VBox(8);
        fila.getStyleClass().add("ventasCarritoItem");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Mini placeholder con gradiente
        StackPane miniImg = new StackPane();
        miniImg.setPrefSize(40, 40);
        miniImg.setMinSize(40, 40);
        miniImg.setMaxSize(40, 40);
        String[] colores = CatalogoSalsiao.gradienteCategoria(item.getProducto().getCategoria());
        miniImg.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 10;",
                colores[0], colores[1]
        ));
        Label miniEmoji = new Label(item.getProducto().getEmoji());
        miniEmoji.setStyle("-fx-font-size: 20px;");
        miniImg.getChildren().add(miniEmoji);

        VBox info = new VBox(2);
        Label nombre = new Label(item.getProducto().getNombre());
        nombre.getStyleClass().add("ventasCarritoItemName");
        Label precioUnit = new Label(item.getProducto().getPrecioFormateado() + " c/u");
        precioUnit.getStyleClass().add("ventasCarritoItemUnit");
        info.getChildren().addAll(nombre, precioUnit);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button btnEliminar = new Button("\u2715");
        btnEliminar.getStyleClass().add("ventasBtnEliminarItem");
        btnEliminar.setOnAction(e -> onEliminar.run());

        header.getChildren().addAll(miniImg, info, btnEliminar);

        HBox controles = new HBox(10);
        controles.setAlignment(Pos.CENTER_LEFT);

        Button btnMenos = new Button("\u2212");
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

        FadeTransition flash = new FadeTransition(Duration.millis(200), card);
        flash.setFromValue(1.0);
        flash.setToValue(0.85);
        flash.setAutoReverse(true);
        flash.setCycleCount(2);
        flash.play();
    }
}
