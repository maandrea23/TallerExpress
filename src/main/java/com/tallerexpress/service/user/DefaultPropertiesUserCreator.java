package com.tallerexpress.service.user;

import com.tallerexpress.model.User;
import java.time.LocalDateTime;

/** Decorador que agrega los valores exigidos sin cambiar el creador base. */
public class DefaultPropertiesUserCreator extends UserCreatorDecorator {
  public DefaultPropertiesUserCreator(UserCreator wrapped) {
    super(wrapped);
  }

  @Override
  public User create(User user) {
    User decorated =
        new User(
            user.id(),
            user.username(),
            user.password(),
            user.fullName(),
            "RECEPCIONISTA",
            "ACTIVO",
            LocalDateTime.now());
    return wrapped.create(decorated);
  }
}
