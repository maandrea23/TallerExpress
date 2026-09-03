package com.tallerexpress.view;
import com.tallerexpress.model.*;
import java.util.*;
import javax.swing.*;

/** Formularios, selección y presentación de vehículos. */
public final class VehicleView {
  public record VehicleData(String plate, String brand, String model, int year) {}
  public int chooseOption() { return DialogView.choose("Vehículos", new String[] {"Registrar", "Listar", "Volver"}); }
  public VehicleData requestVehicle(Client client) {
    JTextField plate = field(""), brand = field(""), model = field(""), year = field("2020");
    if (!DialogView.confirm("Vehículo de " + client.name(), new Object[] {"Placa:", plate,
        "Marca:", brand, "Modelo:", model, "Año:", year})) return null;
    return new VehicleData(plate.getText(), brand.getText(), model.getText(), Integer.parseInt(year.getText()));
  }
  public Vehicle selectVehicle(List<Vehicle> list) {
    if (list.isEmpty()) return null;
    String[] options = list.stream().map(v -> v.id() + " - " + v.plate() + " - " + v.brand()).toArray(String[]::new);
    String selected = (String) JOptionPane.showInputDialog(null, "Vehículo:", "Seleccionar",
        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    return selected == null ? null : list.get(Arrays.asList(options).indexOf(selected));
  }
  public void showVehicles(List<Vehicle> list) { DialogView.show("Vehículos", formatTable(list)); }
  public static String formatTable(List<Vehicle> list) {
    StringBuilder out = new StringBuilder(String.format("%-4s %-7s %-12s %-18s %-18s %s%n",
        "ID", "CLIENTE", "PLACA", "MARCA", "MODELO", "AÑO"));
    for (Vehicle v : list) out.append(String.format("%-4d %-7d %-12s %-18.18s %-18.18s %d%n",
        v.id(), v.clientId(), v.plate(), v.brand(), v.model(), v.year()));
    return out.toString();
  }
  private JTextField field(String value) { return new JTextField(value, 20); }
}
