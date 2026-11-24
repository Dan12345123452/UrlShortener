package org.example.urlshortener.service;

import org.example.urlshortener.model.User;
import org.example.urlshortener.repository.UserRepository;

public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User loginOrRegister(String username) {

    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Введите непустое имя пользователя");
    }

    return userRepository
        .findByUsername(username)
        .orElseGet(() -> userRepository.save(new User(username)));
  }
}
