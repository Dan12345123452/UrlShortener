package org.example.urlshortener.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.example.urlshortener.model.User;

public class UserRepository {

  private final Path filePath;

  private final Map<UUID, User> users = new LinkedHashMap<>();

  public UserRepository(Path filePath) {
    this.filePath = filePath;
    loadUsers();
  }

  public Optional<User> findByUsername(String username) {
    if (username == null) {
      return Optional.empty();
    }

    return users.values().stream()
        .filter(user -> user.getUsername().equalsIgnoreCase(username.trim()))
        .findFirst();
  }

  public User save(User user) {
    users.put(user.getId(), user);
    saveUsers();
    return user;
  }

  private void loadUsers() {
    if (!Files.exists(filePath)) {
      return;
    }

    try {
      List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

      for (String line : lines) {
        String[] parts = line.split(";", 2);

        if (parts.length != 2) {
          continue;
        }

        try {
          UUID id = UUID.fromString(parts[0]);

          String username =
              new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

          User user = new User(id, username);
          users.put(id, user);

        } catch (IllegalArgumentException ignored) {
          // Некорректная строка файла пропускается.
        }
      }

    } catch (IOException e) {
      throw new IllegalStateException("Не удалось прочитать файл пользователей", e);
    }
  }

  private void saveUsers() {
    try {
      Path parent = filePath.getParent();

      if (parent != null) {
        Files.createDirectories(parent);
      }

      List<String> lines = new ArrayList<>();

      for (User user : users.values()) {

        String encodedName =
            Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(user.getUsername().getBytes(StandardCharsets.UTF_8));

        lines.add(user.getId() + ";" + encodedName);
      }

      Files.write(filePath, lines, StandardCharsets.UTF_8);

    } catch (IOException e) {
      throw new IllegalStateException("Не удалось сохранить файл пользователей", e);
    }
  }
}
