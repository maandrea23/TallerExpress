package com.tallerexpress.service.user;


public abstract class UserCreatorDecorator implements UserCreator {
  protected final UserCreator wrapped;

  protected UserCreatorDecorator(UserCreator wrapped) {
    this.wrapped = wrapped;
  }
}
