package org.example.salsiaopf.util;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Métodos auxiliares compartidos entre todos los controladores del sistema.
 * <p>
 * Extraídos para eliminar la duplicaci\u00f3n de {@code cargarLogo()},
 * {@code iniciarReloj()} y {@code animarEntrada()} que aparec\u00eda en
 * 7 controladores distintos con el mismo c\u00f3digo copiado.
 */
public final class ControllerUtil {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("hh:mm a");

    private ControllerUtil() {
    }

    /**
     * Carga el logo circular de Salsiao en un {@link ImageView}.
     */
    public static void cargarLogo(ImageView logoImage) {
        if (logoImage == null) return;
        try {
            var stream = ControllerUtil.class.getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setFitWidth(82);
                logoImage.setFitHeight(82);
                logoImage.setPreserveRatio(true);

                Circle clip = new Circle();
                clip.setRadius(41);
                clip.setCenterX(41);
                clip.setCenterY(41);
                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    /**
     * Inicia un reloj en tiempo real que actualiza dos etiquetas cada segundo.
     */
    public static void iniciarReloj(Label lblFechaActual, Label lblHoraActual) {
        if (lblFechaActual == null || lblHoraActual == null) return;

        Timeline reloj = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> {
                    lblFechaActual.setText(LocalDate.now().format(FORMATO_FECHA));
                    lblHoraActual.setText(LocalTime.now().format(FORMATO_HORA));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    /**
     * Animaci\u00f3n de entrada con fade + slide sobre la ra\u00edz de la escena.
     */
    public static void animarEntrada(Node sceneAnchor) {
        Platform.runLater(() -> {
            if (sceneAnchor == null || sceneAnchor.getScene() == null) return;
            Node root = sceneAnchor.getScene().getRoot();
            if (root == null) return;
            root.setOpacity(0);
            root.setTranslateY(20);
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), root);
            tt.setFromY(20);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition pt = new ParallelTransition(ft, tt);
            pt.setDelay(Duration.millis(80));
            pt.play();
        });
    }
}
