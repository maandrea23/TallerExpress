package com.tallerexpress.controller;

import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.model.*;
import com.tallerexpress.service.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import javax.swing.*;

public class AppController {
  private final WorkshopService workshop = new WorkshopService();
  private final PartService parts = new PartService();
  private User current;

  public void start() {
    login();
    if (current != null) mainMenu();
  }

  private void login() {
    while (current == null) {
      JTextField u = new JTextField("admin");
      JPasswordField p = new JPasswordField();
      int x =
          JOptionPane.showConfirmDialog(
              null,
              new Object[] {"Usuario:", u, "Contraseña:", p},
              "TallerExpress - Iniciar sesión",
              JOptionPane.OK_CANCEL_OPTION);
      if (x != JOptionPane.OK_OPTION) return;
      run(
          () -> {
            current = workshop.login(u.getText(), new String(p.getPassword()));
            return null;
          });
    }
  }

  private void mainMenu() {
    String[] options = {
      "Repuestos", "Clientes", "Vehículos", "Usuarios", "Órdenes de servicio", "Cerrar sesión"
    };
    while (true) {
      int x =
          JOptionPane.showOptionDialog(
              null,
              "Bienvenido, " + current.fullName() + " (" + current.role() + ")",
              "TallerExpress",
              JOptionPane.DEFAULT_OPTION,
              JOptionPane.PLAIN_MESSAGE,
              null,
              options,
              options[0]);
      if (x < 0 || x == 5) return;
      switch (x) {
        case 0 -> partMenu();
        case 1 -> clientMenu();
        case 2 -> vehicleMenu();
        case 3 -> userMenu();
        case 4 -> orderMenu();
      }
    }
  }

  private void partMenu() {
    String[] o = {"Registrar", "Editar", "Listar / filtrar", "Volver"};
    while (true) {
      int x = choose("Repuestos", o);
      if (x < 0 || x == 3) return;
      if (x == 0) partForm(null);
      if (x == 1) {
        Part p = selectPart(parts.list("", ""));
        if (p != null) partForm(p);
      }
      if (x == 2) {
        String c = input("Categoría (vacío = todas)", "");
        if (c != null) {
          String s = input("Proveedor (vacío = todos)", "");
          if (s != null) show("Repuestos", partTable(parts.list(c, s)));
        }
      }
    }
  }

  private void partForm(Part p) {
    JTextField code = f(p == null ? "" : p.code()),
        name = f(p == null ? "" : p.name()),
        cat = f(p == null ? "" : p.category()),
        sup = f(p == null ? "" : p.supplier()),
        total = f(p == null ? "0" : p.stockTotal() + ""),
        avail = f(p == null ? "0" : p.stockAvailable() + ""),
        price = f(p == null ? "0" : p.unitPrice() + "");
    JCheckBox active = new JCheckBox("Activo", p == null || p.active());
    if (confirm(
        "Datos del repuesto",
        new Object[] {
          "Código:",
          code,
          "Nombre:",
          name,
          "Categoría:",
          cat,
          "Proveedor:",
          sup,
          "Stock total:",
          total,
          "Stock disponible:",
          avail,
          "Precio unitario:",
          price,
          active
        }))
      run(
          () ->
              parts.save(
                  new Part(
                      p == null ? null : p.id(),
                      code.getText(),
                      name.getText(),
                      cat.getText(),
                      sup.getText(),
                      Integer.parseInt(total.getText()),
                      Integer.parseInt(avail.getText()),
                      new BigDecimal(price.getText()),
                      active.isSelected(),
                      p == null ? LocalDateTime.now() : p.createdAt())));
  }

  private void clientMenu() {
    String[] o = {"Registrar", "Listar", "Vehículos por cliente", "Volver"};
    while (true) {
      int x = choose("Clientes", o);
      if (x < 0 || x == 3) return;
      if (x == 0) {
        JTextField d = f(""), n = f(""), p = f(""), e = f("");
        if (confirm(
            "Nuevo cliente",
            new Object[] {"Documento:", d, "Nombre:", n, "Teléfono:", p, "Correo:", e}))
          run(() -> workshop.createClient(d.getText(), n.getText(), p.getText(), e.getText()));
      }
      if (x == 1) show("Clientes", clientTable(workshop.clients()));
      if (x == 2) {
        Client c = selectClient();
        if (c != null) show("Vehículos de " + c.name(), vehicleTable(workshop.vehicles(c.id())));
      }
    }
  }

  private void vehicleMenu() {
    String[] o = {"Registrar", "Listar", "Volver"};
    while (true) {
      int x = choose("Vehículos", o);
      if (x < 0 || x == 2) return;
      if (x == 0) {
        Client c = selectClient();
        if (c != null) {
          JTextField p = f(""), b = f(""), m = f(""), y = f("2020");
          if (confirm(
              "Vehículo de " + c.name(),
              new Object[] {"Placa:", p, "Marca:", b, "Modelo:", m, "Año:", y}))
            run(
                () ->
                    workshop.createVehicle(
                        c.id(),
                        p.getText(),
                        b.getText(),
                        m.getText(),
                        Integer.parseInt(y.getText())));
        }
      }
      if (x == 1) show("Vehículos", vehicleTable(workshop.vehicles(null)));
    }
  }

  private void userMenu() {
    if (!"ADMIN".equals(current.role())) {
      error("Solo un ADMIN puede gestionar usuarios");
      return;
    }
    String[] o = {"Registrar", "Listar", "Activar / desactivar", "Eliminar", "Volver"};
    while (true) {
      int x = choose("Usuarios", o);
      if (x < 0 || x == 4) return;
      if (x == 0) {
        JTextField u = f(""), n = f("");
        JPasswordField p = new JPasswordField();
        if (confirm(
            "Nuevo usuario (valores predeterminados: RECEPCIONISTA / ACTIVO)",
            new Object[] {"Usuario:", u, "Contraseña:", p, "Nombre:", n}))
          run(() -> workshop.createUser(u.getText(), new String(p.getPassword()), n.getText()));
      }
      if (x == 1) show("Usuarios", userTable(workshop.users()));
      if (x == 2) {
        String id = input("ID del usuario", "");
        if (id != null)
          run(
              () -> {
                workshop.toggleUser(Long.parseLong(id));
                return null;
              });
      }
      if (x == 3) {
        String id = input("ID del usuario a eliminar", "");
        if (id != null
            && JOptionPane.showConfirmDialog(
                    null, "¿Confirma la eliminación?", "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
          long value = Long.parseLong(id);
          if (value == current.id()) error("No puede eliminar el usuario de la sesión actual");
          else
            run(
                () -> {
                  workshop.deleteUser(value);
                  return null;
                });
        }
      }
    }
  }

  private void orderMenu() {
    String[] o = {"Registrar", "Finalizar", "Listar", "Historial por vehículo", "Volver"};
    while (true) {
      int x = choose("Órdenes", o);
      if (x < 0 || x == 4) return;
      if (x == 0) createOrder();
      if (x == 1) {
        String id = input("ID de orden a finalizar", "");
        if (id != null)
          run(
              () -> {
                BigDecimal total = workshop.finishOrder(Long.parseLong(id));
                show("Orden finalizada", "Costo total: $" + total);
                return null;
              });
      }
      if (x == 2) show("Órdenes", orderTable(workshop.orders(null)));
      if (x == 3) {
        Vehicle v = selectVehicle(workshop.vehicles(null));
        if (v != null) show("Historial " + v.plate(), orderTable(workshop.orders(v.id())));
      }
    }
  }

  private void createOrder() {
    Client c = selectClient();
    if (c == null) return;
    Vehicle v = selectVehicle(workshop.vehicles(c.id()));
    if (v == null) {
      error("El cliente no tiene vehículos registrados");
      return;
    }
    JTextField mechanic = f(""), problem = f(""), diagnosis = f("");
    if (!confirm(
        "Nueva orden",
        new Object[] {"Mecánico:", mechanic, "Problema:", problem, "Diagnóstico:", diagnosis}))
      return;
    List<OrderPart> used = new ArrayList<>();
    while (JOptionPane.showConfirmDialog(
            null, "¿Agregar un repuesto?", "Orden", JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION) {
      Part p = selectPart(parts.list("", ""));
      if (p == null) break;
      String q = input("Cantidad de " + p.name(), "1");
      if (q != null) used.add(new OrderPart(p.id(), Integer.parseInt(q)));
    }
    run(
        () -> {
          long id =
              workshop.createOrder(
                  c.id(), v.id(), mechanic.getText(), problem.getText(), diagnosis.getText(), used);
          show("Éxito", "Orden #" + id + " registrada");
          return id;
        });
  }

  private Client selectClient() {
    List<Client> l = workshop.clients();
    if (l.isEmpty()) {
      error("No hay clientes registrados");
      return null;
    }
    String[] a =
        l.stream()
            .map(c -> c.id() + " - " + c.document() + " - " + c.name())
            .toArray(String[]::new);
    String s =
        (String)
            JOptionPane.showInputDialog(
                null, "Cliente:", "Seleccionar", JOptionPane.PLAIN_MESSAGE, null, a, a[0]);
    return s == null ? null : l.get(Arrays.asList(a).indexOf(s));
  }

  private Vehicle selectVehicle(List<Vehicle> l) {
    if (l.isEmpty()) return null;
    String[] a =
        l.stream().map(v -> v.id() + " - " + v.plate() + " - " + v.brand()).toArray(String[]::new);
    String s =
        (String)
            JOptionPane.showInputDialog(
                null, "Vehículo:", "Seleccionar", JOptionPane.PLAIN_MESSAGE, null, a, a[0]);
    return s == null ? null : l.get(Arrays.asList(a).indexOf(s));
  }

  private Part selectPart(List<Part> l) {
    if (l.isEmpty()) {
      error("No hay repuestos registrados");
      return null;
    }
    String[] a =
        l.stream()
            .map(
                p -> p.id() + " - " + p.code() + " - " + p.name() + " (" + p.stockAvailable() + ")")
            .toArray(String[]::new);
    String s =
        (String)
            JOptionPane.showInputDialog(
                null, "Repuesto:", "Seleccionar", JOptionPane.PLAIN_MESSAGE, null, a, a[0]);
    return s == null ? null : l.get(Arrays.asList(a).indexOf(s));
  }

  private String partTable(List<Part> l) {
    StringBuilder b =
        new StringBuilder(
            String.format(
                "%-4s %-12s %-20s %-14s %-15s %7s %12s %s%n",
                "ID", "CÓDIGO", "NOMBRE", "CATEGORÍA", "PROVEEDOR", "STOCK", "PRECIO", "ESTADO"));
    for (Part p : l)
      b.append(
          String.format(
              "%-4d %-12s %-20.20s %-14.14s %-15.15s %7d %12.2f %s%n",
              p.id(),
              p.code(),
              p.name(),
              p.category(),
              p.supplier(),
              p.stockAvailable(),
              p.unitPrice(),
              p.active() ? "[ACTIVO]" : "[INACTIVO]"));
    return b.toString();
  }

  private String clientTable(List<Client> l) {
    StringBuilder b =
        new StringBuilder(
            String.format(
                "%-4s %-15s %-25s %-15s %s%n", "ID", "DOCUMENTO", "NOMBRE", "TELÉFONO", "ESTADO"));
    for (Client c : l)
      b.append(
          String.format(
              "%-4d %-15s %-25.25s %-15s %s%n",
              c.id(), c.document(), c.name(), c.phone(), c.active() ? "[ACTIVO]" : "[INACTIVO]"));
    return b.toString();
  }

  private String vehicleTable(List<Vehicle> l) {
    StringBuilder b =
        new StringBuilder(
            String.format(
                "%-4s %-7s %-12s %-18s %-18s %s%n",
                "ID", "CLIENTE", "PLACA", "MARCA", "MODELO", "AÑO"));
    for (Vehicle v : l)
      b.append(
          String.format(
              "%-4d %-7d %-12s %-18.18s %-18.18s %d%n",
              v.id(), v.clientId(), v.plate(), v.brand(), v.model(), v.year()));
    return b.toString();
  }

  private String userTable(List<User> l) {
    StringBuilder b =
        new StringBuilder(
            String.format(
                "%-4s %-18s %-25s %-18s %s%n", "ID", "USUARIO", "NOMBRE", "ROL", "ESTADO"));
    for (User u : l)
      b.append(
          String.format(
              "%-4d %-18s %-25.25s %-18s [%s]%n",
              u.id(), u.username(), u.fullName(), u.role(), u.status()));
    return b.toString();
  }

  private String orderTable(List<ServiceOrder> l) {
    StringBuilder b =
        new StringBuilder(
            String.format(
                "%-4s %-7s %-8s %-20s %-12s %12s%n",
                "ID", "CLIENTE", "VEHÍCULO", "MECÁNICO", "ESTADO", "TOTAL"));
    for (ServiceOrder o : l)
      b.append(
          String.format(
              "%-4d %-7d %-8d %-20.20s %-12s %12s%n",
              o.id(),
              o.clientId(),
              o.vehicleId(),
              o.mechanic(),
              o.status(),
              o.finalCost() == null ? "-" : "$" + o.finalCost()));
    return b.toString();
  }

  private <T> T run(Supplier<T> action) {
    try {
      T r = action.get();
      if (r != null) show("Éxito", "Operación realizada correctamente");
      return r;
    } catch (NumberFormatException e) {
      error("Ingrese valores numéricos válidos");
    } catch (BusinessException e) {
      error(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      error("Error inesperado: " + e.getMessage());
    }
    return null;
  }

  private int choose(String title, String[] options) {
    return JOptionPane.showOptionDialog(
        null,
        "Seleccione una opción",
        title,
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.PLAIN_MESSAGE,
        null,
        options,
        options[0]);
  }

  private JTextField f(String s) {
    return new JTextField(s, 20);
  }

  private boolean confirm(String t, Object[] fields) {
    return JOptionPane.showConfirmDialog(null, fields, t, JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION;
  }

  private String input(String m, String d) {
    return JOptionPane.showInputDialog(null, m, d);
  }

  private void show(String t, String m) {
    JTextArea a = new JTextArea(m, Math.min(22, Math.max(5, m.lines().toList().size() + 1)), 95);
    a.setEditable(false);
    a.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
    JOptionPane.showMessageDialog(null, new JScrollPane(a), t, JOptionPane.INFORMATION_MESSAGE);
  }

  private void error(String m) {
    JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
