package com.tallerexpress.view;

import com.tallerexpress.model.User;
import javax.swing.*;

/** Vista del inicio de sesión y menú principal. */
public final class AppView {
  public record Credentials(String username, String password) {}

  public Credentials requestCredentials() {
    JTextField username = new JTextField("admin");
    JPasswordField password = new JPasswordField();
    if (!DialogView.confirm("TallerExpress - Iniciar sesión",
        new Object[] {"Usuario:", username, "Contraseña:", password})) return null;
    return new Credentials(username.getText(), new String(password.getPassword()));
  }

  public int chooseMainOption(User user) {
    String[] options = {"Repuestos", "Clientes", "Vehículos", "Usuarios",
        "Órdenes de servicio", "Cerrar sesión"};
    return JOptionPane.showOptionDialog(null,
        "Bienvenido, " + user.fullName() + " (" + user.role() + ")", "TallerExpress",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
  }
}
