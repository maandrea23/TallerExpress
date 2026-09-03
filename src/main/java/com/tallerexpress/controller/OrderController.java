package com.tallerexpress.controller;
import com.tallerexpress.model.*;
import com.tallerexpress.service.*;
import com.tallerexpress.view.*;
import java.math.BigDecimal;
import java.util.*;

/** Coordina la vista de órdenes con sus reglas de negocio. */
final class OrderController extends BaseController {
  private final WorkshopService service;
  private final PartService parts;
  private final ClientController clients;
  private final VehicleController vehicles;
  private final PartController partController;
  private final OrderView view = new OrderView();
  OrderController(WorkshopService service, PartService parts, ClientController clients,
      VehicleController vehicles, PartController partController) {
    this.service = service; this.parts = parts; this.clients = clients;
    this.vehicles = vehicles; this.partController = partController;
  }
  void showMenu() {
    while (true) {
      int option = view.chooseOption();
      if (option < 0 || option == 4) return;
      if (option == 0) create();
      if (option == 1) finish();
      if (option == 2) view.showOrders("Órdenes", service.orders(null));
      if (option == 3) showHistory();
    }
  }
  private void finish() {
    String id = view.requestOrderId();
    if (id != null) run(() -> { BigDecimal total = service.finishOrder(Long.parseLong(id)); view.showTotal(total); return null; });
  }
  private void showHistory() {
    Vehicle vehicle = vehicles.select(service.vehicles(null));
    if (vehicle != null) view.showOrders("Historial " + vehicle.plate(), service.orders(vehicle.id()));
  }
  private void create() {
    Client client = clients.select();
    if (client == null) return;
    Vehicle vehicle = vehicles.select(service.vehicles(client.id()));
    if (vehicle == null) { DialogView.error("El cliente no tiene vehículos registrados"); return; }
    OrderView.OrderData data = view.requestOrder();
    if (data == null) return;
    List<OrderPart> used = new ArrayList<>();
    try {
      while (view.confirmAddPart()) {
        Part part = partController.select(parts.list("", ""));
        if (part == null) break;
        String quantity = view.requestQuantity(part);
        if (quantity != null) used.add(new OrderPart(part.id(), Integer.parseInt(quantity)));
      }
    } catch (NumberFormatException e) { DialogView.error("Ingrese valores numéricos válidos"); return; }
    run(() -> {
      long id = service.createOrder(client.id(), vehicle.id(), data.mechanic(), data.problem(), data.diagnosis(), used);
      view.showCreated(id); return id;
    });
  }
}
