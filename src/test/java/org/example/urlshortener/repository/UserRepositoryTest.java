package org.example.urlshortener.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.example.urlshortener.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

  @TempDir Path tempDir;

  @Test
  void shouldSaveAndLoadUser() {

    Path file = tempDir.resolve("users.txt");

    User user = new User("Alex");

    UserRepository repository = new UserRepository(file);

    repository.save(user);

    UserRepository loadedRepository = new UserRepository(file);

    User loadedUser = loadedRepository.findByUsername("Alex").orElseThrow();

    assertEquals(user.getId(), loadedUser.getId());
    assertEquals(user.getUsername(), loadedUser.getUsername());
  }

  @Test
  void shouldFindUsernameIgnoringCase() {

    Path file = tempDir.resolve("users.txt");

    UserRepository repository = new UserRepository(file);

    repository.save(new User("Alex"));

    assertTrue(repository.findByUsername("ALEX").isPresent());
  }

  @Test
  void shouldReturnEmptyForUnknownUser() {

    UserRepository repository = new UserRepository(tempDir.resolve("users.txt"));

    assertTrue(repository.findByUsername("Unknown").isEmpty());
  }

  @Test
  void shouldReturnEmptyForNullUsername() {

    UserRepository repository = new UserRepository(tempDir.resolve("users.txt"));

    assertTrue(repository.findByUsername(null).isEmpty());
  }

  @Test
  void shouldIgnoreMalformedFileEntries() throws Exception {

    Path file = tempDir.resolve("users.txt");

    Files.write(file, List.of("broken-line", "not-a-uuid;YWxleA", "another;%%%"));

    UserRepository repository = new UserRepository(file);

    assertTrue(repository.findByUsername("Alex").isEmpty());
  }
}
