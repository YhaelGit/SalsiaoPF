package org.example.salsiaopf;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segOrder;
    @FXML private ToggleButton segSupplier;
    @FXML private ToggleButton segReception;
    @FXML private ToggleButton segIngredient;
    @FXML private ToggleButton segPayment;

    @FXML private VBox viewOrder;
    @FXML private VBox viewSupplier;
    @FXML private VBox viewReception;
    @FXML private VBox viewIngredient;
    @FXML private VBox viewPayment;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        URL logoUrl = getClass().getResource("/imagenes/logo-salsiao.jpeg");
        if (logoUrl != null && logoImage != null) {
            logoImage.setImage(new Image(logoUrl.toExternalForm()));
        }

        if (segOrder != null) segOrder.setSelected(true);
        showOnly(viewOrder);
    }

    private void showOnly(VBox target) {
        VBox[] all = { viewOrder, viewSupplier, viewReception, viewIngredient, viewPayment };
        for (VBox v : all) {
            boolean on = (v == target);
            v.setVisible(on);
            v.setManaged(on);
        }
    }

    @FXML private void showOrder() { showOnly(viewOrder); }
    @FXML private void showSupplier() { showOnly(viewSupplier); }
    @FXML private void showReception() { showOnly(viewReception); }
    @FXML private void showIngredient() { showOnly(viewIngredient); }
    @FXML private void showPayment() { showOnly(viewPayment); }
}
