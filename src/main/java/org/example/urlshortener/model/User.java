package org.example.urlshortener.model;

import java.util.UUID;

public class User {

  private final UUID id;
  private final String username;

  public User(String username) {
    this(UUID.randomUUID(), username);
  }

  public User(UUID id, String username) {

    if (id == null) {
      throw new IllegalArgumentException("UUID пользователя не может быть пустым");
    }

    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Имя пользователя не может быть пустым");
    }

    this.id = id;
    this.username = username.trim();
  }

  public UUID getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }
}
