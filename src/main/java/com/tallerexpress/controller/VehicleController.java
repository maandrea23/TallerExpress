package com.tallerexpress.controller;
import com.tallerexpress.model.*;
import com.tallerexpress.service.WorkshopService;
import com.tallerexpress.view.*;
import java.util.List;

/** Coordina la vista de vehículos con sus reglas de negocio. */
final class VehicleController extends BaseController {
  private final WorkshopService service;
  private final ClientController clients;
  private final VehicleView view = new VehicleView();
  VehicleController(WorkshopService service, ClientController clients) { this.service = service; this.clients = clients; }
  void showMenu() {
    while (true) {
      int option = view.chooseOption();
      if (option < 0 || option == 2) return;
      if (option == 0) create();
      if (option == 1) view.showVehicles(service.vehicles(null));
    }
  }
  Vehicle select(List<Vehicle> list) { return view.selectVehicle(list); }
  private void create() {
    Client client = clients.select();
    if (client == null) return;
    VehicleView.VehicleData data = view.requestVehicle(client);
    if (data != null) run(() -> service.createVehicle(client.id(), data.plate(), data.brand(), data.model(), data.year()));
  }
}
