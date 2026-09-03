package com.tallerexpress.view;
import com.tallerexpress.model.*;
import java.util.*;
import javax.swing.*;

/** Formularios, selección y presentación de clientes. */
public final class ClientView {
  public record ClientData(String document, String name, String phone, String email) {}
  public int chooseOption() { return DialogView.choose("Clientes",
      new String[] {"Registrar", "Listar", "Vehículos por cliente", "Volver"}); }
  public ClientData requestClient() {
    JTextField document = field(), name = field(), phone = field(), email = field();
    if (!DialogView.confirm("Nuevo cliente", new Object[] {"Documento:", document, "Nombre:", name,
        "Teléfono:", phone, "Correo:", email})) return null;
    return new ClientData(document.getText(), name.getText(), phone.getText(), email.getText());
  }
  public Client selectClient(List<Client> list) {
    if (list.isEmpty()) { DialogView.error("No hay clientes registrados"); return null; }
    String[] options = list.stream().map(c -> c.id() + " - " + c.document() + " - " + c.name()).toArray(String[]::new);
    String selected = (String) JOptionPane.showInputDialog(null, "Cliente:", "Seleccionar",
        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    return selected == null ? null : list.get(Arrays.asList(options).indexOf(selected));
  }
  public void showClients(List<Client> list) {
    StringBuilder out = new StringBuilder(String.format("%-4s %-15s %-25s %-15s %s%n",
        "ID", "DOCUMENTO", "NOMBRE", "TELÉFONO", "ESTADO"));
    for (Client c : list) out.append(String.format("%-4d %-15s %-25.25s %-15s %s%n", c.id(),
        c.document(), c.name(), c.phone(), c.active() ? "[ACTIVO]" : "[INACTIVO]"));
    DialogView.show("Clientes", out.toString());
  }
  public void showClientVehicles(Client client, List<Vehicle> vehicles) {
    DialogView.show("Vehículos de " + client.name(), VehicleView.formatTable(vehicles));
  }
  private JTextField field() { return new JTextField("", 20); }
}
