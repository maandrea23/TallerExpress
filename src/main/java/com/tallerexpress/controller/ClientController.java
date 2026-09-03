package com.tallerexpress.controller;
import com.tallerexpress.model.Client;
import com.tallerexpress.service.WorkshopService;
import com.tallerexpress.view.ClientView;

/** Coordina la vista de clientes con sus reglas de negocio. */
final class ClientController extends BaseController {
  private final WorkshopService service;
  private final ClientView view = new ClientView();
  ClientController(WorkshopService service) { this.service = service; }
  void showMenu() {
    while (true) {
      int option = view.chooseOption();
      if (option < 0 || option == 3) return;
      if (option == 0) {
        ClientView.ClientData data = view.requestClient();
        if (data != null) run(() -> service.createClient(data.document(), data.name(), data.phone(), data.email()));
      }
      if (option == 1) view.showClients(service.clients());
      if (option == 2) { Client c = select(); if (c != null) view.showClientVehicles(c, service.vehicles(c.id())); }
    }
  }
  Client select() { return view.selectClient(service.clients()); }
}
