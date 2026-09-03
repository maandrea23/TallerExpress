package com.tallerexpress.view;
import com.tallerexpress.model.Part;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;

/** Formularios, selección y presentación de repuestos. */
public final class PartView {
  public int chooseOption() { return DialogView.choose("Repuestos",
      new String[] {"Registrar", "Editar", "Listar / filtrar", "Volver"}); }
  public String[] requestFilters() {
    String category = DialogView.input("Categoría (vacío = todas)", "");
    if (category == null) return null;
    String supplier = DialogView.input("Proveedor (vacío = todos)", "");
    return supplier == null ? null : new String[] {category, supplier};
  }
  public Part selectPart(List<Part> list) {
    if (list.isEmpty()) { DialogView.error("No hay repuestos registrados"); return null; }
    String[] options = list.stream().map(p -> p.id() + " - " + p.code() + " - " + p.name()
        + " (" + p.stockAvailable() + ")").toArray(String[]::new);
    String selected = (String) JOptionPane.showInputDialog(null, "Repuesto:", "Seleccionar",
        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    return selected == null ? null : list.get(Arrays.asList(options).indexOf(selected));
  }
  public Part requestPart(Part current) {
    JTextField code = field(current == null ? "" : current.code()), name = field(current == null ? "" : current.name());
    JTextField category = field(current == null ? "" : current.category()), supplier = field(current == null ? "" : current.supplier());
    JTextField total = field(current == null ? "0" : String.valueOf(current.stockTotal()));
    JTextField available = field(current == null ? "0" : String.valueOf(current.stockAvailable()));
    JTextField price = field(current == null ? "0" : current.unitPrice().toString());
    JCheckBox active = new JCheckBox("Activo", current == null || current.active());
    if (!DialogView.confirm("Datos del repuesto", new Object[] {"Código:", code, "Nombre:", name,
        "Categoría:", category, "Proveedor:", supplier, "Stock total:", total,
        "Stock disponible:", available, "Precio unitario:", price, active})) return null;
    return new Part(current == null ? null : current.id(), code.getText(), name.getText(), category.getText(),
        supplier.getText(), Integer.parseInt(total.getText()), Integer.parseInt(available.getText()),
        new BigDecimal(price.getText()), active.isSelected(), current == null ? LocalDateTime.now() : current.createdAt());
  }
  public void showParts(List<Part> list) {
    StringBuilder out = new StringBuilder(String.format("%-4s %-12s %-20s %-14s %-15s %7s %12s %s%n",
        "ID", "CÓDIGO", "NOMBRE", "CATEGORÍA", "PROVEEDOR", "STOCK", "PRECIO", "ESTADO"));
    for (Part p : list) out.append(String.format("%-4d %-12s %-20.20s %-14.14s %-15.15s %7d %12.2f %s%n",
        p.id(), p.code(), p.name(), p.category(), p.supplier(), p.stockAvailable(), p.unitPrice(), p.active() ? "[ACTIVO]" : "[INACTIVO]"));
    DialogView.show("Repuestos", out.toString());
  }
  private JTextField field(String value) { return new JTextField(value, 20); }
}
