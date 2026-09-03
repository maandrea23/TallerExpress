package com.tallerexpress.controller;

import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.view.DialogView;
import java.util.function.Supplier;

/** Manejo común de resultados y errores de los controladores. */
abstract class BaseController {
  protected <T> T run(Supplier<T> action) {
    try {
      T result = action.get();
      if (result != null) DialogView.success();
      return result;
    } catch (NumberFormatException exception) {
      DialogView.error("Ingrese valores numéricos válidos");
    } catch (BusinessException exception) {
      DialogView.error(exception.getMessage());
    } catch (Exception exception) {
      exception.printStackTrace();
      DialogView.error("Error inesperado: " + exception.getMessage());
    }
    return null;
  }
}
