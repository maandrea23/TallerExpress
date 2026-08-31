package com.tallerexpress.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Inicializa y verifica la base de datos sin abrir la interfaz gráfica. */
public final class DatabaseSetup {

  private DatabaseSetup() {}

  public static void main(String[] args) throws Exception {
    Database.initialize();

    try (Connection connection = Database.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")) {
      resultSet.next();
      System.out.println("PostgreSQL conectado correctamente.");
      System.out.println("Tablas encontradas: " + resultSet.getInt(1));
    }
  }
}
