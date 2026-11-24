package org.example.urlshortener.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void shouldGenerateUuidAndTrimUsername() {

    User user = new User("  Alex  ");

    assertNotNull(user.getId());
    assertEquals("Alex", user.getUsername());
  }

  @Test
  void shouldUseProvidedUuid() {

    UUID id = UUID.randomUUID();

    User user = new User(id, "Alex");

    assertEquals(id, user.getId());
  }

  @Test
  void shouldRejectNullUuid() {

    assertThrows(IllegalArgumentException.class, () -> new User(null, "Alex"));
  }

  @Test
  void shouldRejectBlankUsername() {

    assertThrows(IllegalArgumentException.class, () -> new User("   "));
  }
}
