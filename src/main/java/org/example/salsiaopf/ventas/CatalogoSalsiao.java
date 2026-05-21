package org.example.salsiaopf.ventas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Catálogo oficial del menú Salsiao para el módulo de ventas.
 */
public final class CatalogoSalsiao {

    public static final String TODAS = "Todos";

    public static final List<String> CATEGORIAS = Arrays.asList(
            TODAS,
            "Hamburguesas",
            "Hotdogs",
            "Pizzas",
            "Tacos",
            "Wraps",
            "Sándwiches",
            "Pollo / Alitas",
            "Picaderas",
            "Papas y extras",
            "Bebidas",
            "Postres",
            "Combos"
    );

    private static final List<ProductoMenu> PRODUCTOS = new ArrayList<>();

    static {
        // Hamburguesas
        agregar("burg-1", "La Salsiao", "La estrella de la casa, jugosa y cargada.", "Hamburguesas", "🍔", 620);
        agregar("burg-2", "La Callejera", "Sabor urbano, rápida y contundente.", "Hamburguesas", "🍔", 450);
        agregar("burg-3", "La Chapiadora", "Premium con extras que impactan.", "Hamburguesas", "🍔", 690);
        agregar("burg-4", "BBQ Monster", "Ahumada, BBQ intenso, doble sabor.", "Hamburguesas", "🍔", 590);
        agregar("burg-5", "Crispy Chicken Burger", "Pollo crujiente, salsa especial.", "Hamburguesas", "🍔", 480);

        // Hotdogs
        agregar("hd-1", "Clásico", "El de siempre, rápido y confiable.", "Hotdogs", "🌭", 220);
        agregar("hd-2", "Callejero", "Con toppings callejeros.", "Hotdogs", "🌭", 290);
        agregar("hd-3", "XL", "Tamaño XL para hambre grande.", "Hotdogs", "🌭", 360);

        // Pizzas
        agregar("piz-1", "Personal", "Pizza personal, masa fresca.", "Pizzas", "🍕", 445);
        agregar("piz-2", "Mediana", "Ideal para compartir en mesa.", "Pizzas", "🍕", 790);
        agregar("piz-3", "Familiar", "Familiar XL, fiesta en la mesa.", "Pizzas", "🍕", 1050);

        // Tacos
        agregar("tac-1", "Crispy", "Taco crujiente con relleno premium.", "Tacos", "🌮", 360);
        agregar("tac-2", "BBQ", "Taco BBQ con toque ahumado.", "Tacos", "🌮", 390);

        // Wraps
        agregar("wr-1", "Crispy Chicken", "Wrap de pollo crispy.", "Wraps", "🌯", 410);
        agregar("wr-2", "BBQ", "Wrap BBQ con queso fundido.", "Wraps", "🌯", 430);

        // Sándwiches
        agregar("san-1", "Club", "Club sandwich completo.", "Sándwiches", "🥪", 540);
        agregar("san-2", "Philly Cheesesteak", "Philly con queso derretido.", "Sándwiches", "🥪", 590);
        agregar("san-3", "Crispy", "Sándwich crispy de pollo.", "Sándwiches", "🥪", 420);

        // Pollo / Alitas
        agregar("ali-1", "6 piezas", "Alitas bañadas, porción 6.", "Pollo / Alitas", "🍗", 495);
        agregar("ali-2", "12 piezas", "Ideal para compartir.", "Pollo / Alitas", "🍗", 895);
        agregar("ali-3", "24 piezas", "Party pack de alitas.", "Pollo / Alitas", "🍗", 1690);

        // Picaderas
        agregar("pic-1", "Nachos Salsiao", "Nachos con queso y salsa house.", "Picaderas", "🧀", 320);
        agregar("pic-2", "Mozzarella Sticks", "Palitos de mozzarella crujientes.", "Picaderas", "🧀", 280);

        // Papas y extras
        agregar("pap-1", "Clásicas", "Papas fritas doradas.", "Papas y extras", "🍟", 120);
        agregar("pap-2", "Bacon Cheese Fries", "Con bacon y queso.", "Papas y extras", "🍟", 250);
        agregar("pap-3", "Loaded Fries", "Cargadas con toppings.", "Papas y extras", "🍟", 290);

        // Bebidas
        agregar("beb-1", "Refresco Cola", "Frío, burbujeante.", "Bebidas", "🥤", 75);
        agregar("beb-2", "Refresco Sprite", "Lima-limón refrescante.", "Bebidas", "🥤", 75);
        agregar("beb-3", "Refresco Uva", "Sabor uva intenso.", "Bebidas", "🥤", 65);
        agregar("beb-4", "Batida Fresa", "Batida cremosa de fresa.", "Bebidas", "🥤", 220);
        agregar("beb-5", "Batida Mango", "Tropical, mango fresco.", "Bebidas", "🥤", 240);
        agregar("beb-6", "Batida Oreo", "Batida Oreo indulgente.", "Bebidas", "🥤", 180);

        // Postres
        agregar("pos-1", "Brownie", "Brownie de chocolate caliente.", "Postres", "🍰", 180);
        agregar("pos-2", "Cheesecake", "Cheesecake cremoso.", "Postres", "🍰", 250);

        // Combos
        agregar("com-1", "Combo Street", "Combo callejero completo.", "Combos", "📦", 690);
        agregar("com-2", "Combo Crispy", "Combo crispy chicken.", "Combos", "📦", 590);
        agregar("com-3", "Combo Party", "Combo fiesta para grupo.", "Combos", "📦", 1890);
    }

    private CatalogoSalsiao() {
    }

    private static void agregar(String id, String nombre, String desc, String cat, String emoji, double precio) {
        PRODUCTOS.add(new ProductoMenu(id, nombre, desc, cat, emoji, precio));
    }

    public static List<ProductoMenu> todos() {
        return List.copyOf(PRODUCTOS);
    }

    public static List<ProductoMenu> porCategoria(String categoria) {
        if (categoria == null || TODAS.equals(categoria)) {
            return todos();
        }
        return PRODUCTOS.stream()
                .filter(p -> p.getCategoria().equals(categoria))
                .collect(Collectors.toList());
    }

    public static List<ProductoMenu> buscar(String texto, String categoria) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        return porCategoria(categoria).stream()
                .filter(p -> filtro.isEmpty()
                        || p.getNombre().toLowerCase().contains(filtro)
                        || p.getDescripcion().toLowerCase().contains(filtro)
                        || p.getCategoria().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }
}
