package com.tallerexpress;

import com.tallerexpress.config.Database;
import com.tallerexpress.controller.AppController;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

  private Main() {}

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      Database.initialize();

      SwingUtilities.invokeLater(
          () -> {
            AppController controller = new AppController();
            controller.start();
          });
    } catch (Exception exception) {
      exception.printStackTrace();

      JOptionPane.showMessageDialog(
          null,
          "No fue posible iniciar TallerExpress: " + exception.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
