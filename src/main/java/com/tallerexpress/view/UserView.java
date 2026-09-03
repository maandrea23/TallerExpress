package com.tallerexpress.view;
import com.tallerexpress.model.User;
import java.util.List;
import javax.swing.*;

/** Formularios y presentación de usuarios. */
public final class UserView {
  public record UserData(String username, String password, String fullName) {}
  public int chooseOption() { return DialogView.choose("Usuarios",
      new String[] {"Registrar", "Listar", "Activar / desactivar", "Eliminar", "Volver"}); }
  public UserData requestUser() {
    JTextField username = new JTextField("", 20), name = new JTextField("", 20);
    JPasswordField password = new JPasswordField();
    if (!DialogView.confirm("Nuevo usuario (valores predeterminados: RECEPCIONISTA / ACTIVO)",
        new Object[] {"Usuario:", username, "Contraseña:", password, "Nombre:", name})) return null;
    return new UserData(username.getText(), new String(password.getPassword()), name.getText());
  }
  public String requestUserId(String message) { return DialogView.input(message, ""); }
  public boolean confirmDeletion() {
    return JOptionPane.showConfirmDialog(null, "¿Confirma la eliminación?", "Confirmar",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }
  public void showUsers(List<User> list) {
    StringBuilder out = new StringBuilder(String.format("%-4s %-18s %-25s %-18s %s%n",
        "ID", "USUARIO", "NOMBRE", "ROL", "ESTADO"));
    for (User u : list) out.append(String.format("%-4d %-18s %-25.25s %-18s [%s]%n",
        u.id(), u.username(), u.fullName(), u.role(), u.status()));
    DialogView.show("Usuarios", out.toString());
  }
}
