package com.tallerexpress.controller;

import com.tallerexpress.model.User;
import com.tallerexpress.service.PartService;
import com.tallerexpress.service.WorkshopService;
import com.tallerexpress.view.AppView;

/** Coordina el inicio de sesión y el menú principal. */
public class AppController extends BaseController {
  private final WorkshopService workshop = new WorkshopService();
  private final PartService parts = new PartService();
  private final ClientController clients = new ClientController(workshop);
  private final VehicleController vehicles = new VehicleController(workshop, clients);
  private final PartController partController = new PartController(parts);
  private final UserController users = new UserController(workshop);
  private final OrderController orders =
      new OrderController(workshop, parts, clients, vehicles, partController);
  private final AppView view = new AppView();
  private User current;

  public void start() {
    login();
    if (current != null) mainMenu();
  }

  private void login() {
    while (current == null) {
      AppView.Credentials credentials = view.requestCredentials();
      if (credentials == null) return;
      run(() -> {
        current = workshop.login(credentials.username(), credentials.password());
        return null;
      });
    }
  }

  private void mainMenu() {
    while (true) {
      int option = view.chooseMainOption(current);
      if (option < 0 || option == 5) return;
      switch (option) {
        case 0 -> partController.showMenu();
        case 1 -> clients.showMenu();
        case 2 -> vehicles.showMenu();
        case 3 -> users.showMenu(current);
        case 4 -> orders.showMenu();
        default -> { }
      }
    }
  }
}
