package com.tallerexpress.view;

import java.awt.Font;
import javax.swing.*;

/** Componentes visuales comunes de la aplicación. */
public final class DialogView {
  private DialogView() {}

  public static int choose(String title, String[] options) {
    return JOptionPane.showOptionDialog(null, "Seleccione una opción", title,
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
  }

  public static String input(String message, String defaultValue) {
    return JOptionPane.showInputDialog(null, message, defaultValue);
  }

  public static boolean confirm(String title, Object[] fields) {
    return JOptionPane.showConfirmDialog(null, fields, title, JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION;
  }

  public static void show(String title, String message) {
    int rows = Math.min(22, Math.max(5, (int) message.lines().count() + 1));
    JTextArea area = new JTextArea(message, rows, 95);
    area.setEditable(false);
    area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    JOptionPane.showMessageDialog(null, new JScrollPane(area), title,
        JOptionPane.INFORMATION_MESSAGE);
  }

  public static void success() { show("Éxito", "Operación realizada correctamente"); }

  public static void error(String message) {
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
  }
}
