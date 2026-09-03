package com.tallerexpress.controller;
import com.tallerexpress.model.Part;
import com.tallerexpress.service.PartService;
import com.tallerexpress.view.PartView;
import java.util.List;

/** Coordina la vista de repuestos con sus reglas de negocio. */
final class PartController extends BaseController {
  private final PartService service;
  private final PartView view = new PartView();
  PartController(PartService service) { this.service = service; }
  void showMenu() {
    while (true) {
      int option = view.chooseOption();
      if (option < 0 || option == 3) return;
      if (option == 0) save(null);
      if (option == 1) { Part part = select(service.list("", "")); if (part != null) save(part); }
      if (option == 2) {
        String[] filters = view.requestFilters();
        if (filters != null) view.showParts(service.list(filters[0], filters[1]));
      }
    }
  }
  Part select(List<Part> list) { return view.selectPart(list); }
  private void save(Part current) {
    try {
      Part part = view.requestPart(current);
      if (part != null) run(() -> service.save(part));
    } catch (NumberFormatException e) { com.tallerexpress.view.DialogView.error("Ingrese valores numéricos válidos"); }
  }
}
