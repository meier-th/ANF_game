package com.anf.model;

import java.security.Principal;

public class StompPrincipal implements Principal {

  private String name;

  public StompPrincipal(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
