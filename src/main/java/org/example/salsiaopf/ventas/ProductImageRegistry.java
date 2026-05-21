package org.example.salsiaopf.ventas;

import java.util.HashMap;
import java.util.Map;

public final class ProductImageRegistry {

    private static final Map<String, String> IMAGES = new HashMap<>();
    private static final String DEFAULT = "/assets/products/default.jpg";

    static {
        IMAGES.put("burg-1", "/assets/products/burgers/la_salsiao.jpg");
        IMAGES.put("burg-2", "/assets/products/burgers/la_callejera.jpg");
        IMAGES.put("burg-3", "/assets/products/burgers/la_chapiadora.jpg");
        IMAGES.put("burg-4", "/assets/products/burgers/bbq_monster.jpg");
        IMAGES.put("burg-5", "/assets/products/burgers/crispy_chicken_burger.jpg");

        IMAGES.put("hd-1", "/assets/products/hotdogs/clasico.jpg");
        IMAGES.put("hd-2", "/assets/products/hotdogs/callejero.jpg");
        IMAGES.put("hd-3", "/assets/products/hotdogs/xl.jpg");

        IMAGES.put("piz-1", "/assets/products/pizzas/personal.jpg");
        IMAGES.put("piz-2", "/assets/products/pizzas/mediana.jpg");
        IMAGES.put("piz-3", "/assets/products/pizzas/familiar.jpg");

        IMAGES.put("tac-1", "/assets/products/tacos/crispy.jpg");
        IMAGES.put("tac-2", "/assets/products/tacos/bbq.jpg");

        IMAGES.put("wr-1", "/assets/products/wraps/crispy_chicken.jpg");
        IMAGES.put("wr-2", "/assets/products/wraps/bbq.jpg");

        IMAGES.put("san-1", "/assets/products/sandwiches/club.jpg");
        IMAGES.put("san-2", "/assets/products/sandwiches/philly_cheesesteak.jpg");
        IMAGES.put("san-3", "/assets/products/sandwiches/crispy.jpg");

        IMAGES.put("ali-1", "/assets/products/alitas/6_piezas.jpg");
        IMAGES.put("ali-2", "/assets/products/alitas/12_piezas.jpg");
        IMAGES.put("ali-3", "/assets/products/alitas/24_piezas.jpg");

        IMAGES.put("pic-1", "/assets/products/picaderas/nachos_salsiao.jpg");
        IMAGES.put("pic-2", "/assets/products/picaderas/mozzarella_sticks.jpg");

        IMAGES.put("pap-1", "/assets/products/papas/clasicas.jpg");
        IMAGES.put("pap-2", "/assets/products/papas/bacon_cheese_fries.jpg");
        IMAGES.put("pap-3", "/assets/products/papas/loaded_fries.jpg");

        IMAGES.put("beb-1", "/assets/products/bebidas/refresco_cola.jpg");
        IMAGES.put("beb-2", "/assets/products/bebidas/refresco_sprite.jpg");
        IMAGES.put("beb-3", "/assets/products/bebidas/refresco_uva.jpg");
        IMAGES.put("beb-4", "/assets/products/bebidas/batida_fresa.jpg");
        IMAGES.put("beb-5", "/assets/products/bebidas/batida_mango.jpg");
        IMAGES.put("beb-6", "/assets/products/bebidas/batida_oreo.jpg");

        IMAGES.put("pos-1", "/assets/products/postres/brownie.jpg");
        IMAGES.put("pos-2", "/assets/products/postres/cheesecake.jpg");
    }

    private ProductImageRegistry() {
    }

    public static String get(String productId) {
        return IMAGES.getOrDefault(productId, DEFAULT);
    }
}
