package com.tallerexpress.view;
import com.tallerexpress.model.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;

/** Formularios y presentación de órdenes de servicio. */
public final class OrderView {
  public record OrderData(String mechanic, String problem, String diagnosis) {}
  public int chooseOption() { return DialogView.choose("Órdenes",
      new String[] {"Registrar", "Finalizar", "Listar", "Historial por vehículo", "Volver"}); }
  public String requestOrderId() { return DialogView.input("ID de orden a finalizar", ""); }
  public OrderData requestOrder() {
    JTextField mechanic = field(), problem = field(), diagnosis = field();
    if (!DialogView.confirm("Nueva orden", new Object[] {"Mecánico:", mechanic,
        "Problema:", problem, "Diagnóstico:", diagnosis})) return null;
    return new OrderData(mechanic.getText(), problem.getText(), diagnosis.getText());
  }
  public boolean confirmAddPart() {
    return JOptionPane.showConfirmDialog(null, "¿Agregar un repuesto?", "Orden",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }
  public String requestQuantity(Part part) { return DialogView.input("Cantidad de " + part.name(), "1"); }
  public void showTotal(BigDecimal total) { DialogView.show("Orden finalizada", "Costo total: $" + total); }
  public void showCreated(long id) { DialogView.show("Éxito", "Orden #" + id + " registrada"); }
  public void showOrders(String title, List<ServiceOrder> list) {
    StringBuilder out = new StringBuilder(String.format("%-4s %-7s %-8s %-20s %-12s %12s%n",
        "ID", "CLIENTE", "VEHÍCULO", "MECÁNICO", "ESTADO", "TOTAL"));
    for (ServiceOrder o : list) out.append(String.format("%-4d %-7d %-8d %-20.20s %-12s %12s%n",
        o.id(), o.clientId(), o.vehicleId(), o.mechanic(), o.status(), o.finalCost() == null ? "-" : "$" + o.finalCost()));
    DialogView.show(title, out.toString());
  }
  private JTextField field() { return new JTextField("", 20); }
}
