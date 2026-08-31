package com.tallerexpress.service.user;

import com.tallerexpress.model.User;

public class BaseUserCreator implements UserCreator {
  @Override
  public User create(User user) {
    return user;
  }
}
