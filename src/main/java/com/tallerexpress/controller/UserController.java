package com.tallerexpress.controller;
import com.tallerexpress.model.User;
import com.tallerexpress.service.WorkshopService;
import com.tallerexpress.view.*;

/** Coordina la vista de usuarios con sus reglas de negocio. */
final class UserController extends BaseController {
  private final WorkshopService service;
  private final UserView view = new UserView();
  UserController(WorkshopService service) { this.service = service; }
  void showMenu(User current) {
    if (!"ADMIN".equals(current.role())) { DialogView.error("Solo un ADMIN puede gestionar usuarios"); return; }
    while (true) {
      int option = view.chooseOption();
      if (option < 0 || option == 4) return;
      if (option == 0) create();
      if (option == 1) view.showUsers(service.users());
      if (option == 2) toggle();
      if (option == 3) delete(current);
    }
  }
  private void create() {
    UserView.UserData data = view.requestUser();
    if (data != null) run(() -> service.createUser(data.username(), data.password(), data.fullName()));
  }
  private void toggle() {
    String id = view.requestUserId("ID del usuario");
    if (id != null) run(() -> { service.toggleUser(Long.parseLong(id)); return null; });
  }
  private void delete(User current) {
    String id = view.requestUserId("ID del usuario a eliminar");
    if (id == null || !view.confirmDeletion()) return;
    try {
      long value = Long.parseLong(id);
      if (value == current.id()) DialogView.error("No puede eliminar el usuario de la sesión actual");
      else run(() -> { service.deleteUser(value); return null; });
    } catch (NumberFormatException e) { DialogView.error("Ingrese valores numéricos válidos"); }
  }
}
