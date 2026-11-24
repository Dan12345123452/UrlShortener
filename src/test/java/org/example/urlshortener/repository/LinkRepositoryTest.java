package org.example.urlshortener.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.example.urlshortener.model.ShortLink;
import org.junit.jupiter.api.Test;

class LinkRepositoryTest {

  private final LinkRepository repository = new LinkRepository();

  private ShortLink createLink(String code, UUID ownerId, Instant expiresAt) {

    // Дата создания всегда на час раньше окончания ссылки
    Instant createdAt = expiresAt.minusSeconds(3600);

    return new ShortLink(code, "https://example.com", ownerId, createdAt, expiresAt, 5);
  }

  @Test
  void shouldSaveAndFindLink() {

    UUID ownerId = UUID.randomUUID();

    ShortLink link = createLink("abc1234", ownerId, Instant.now().plusSeconds(3600));

    repository.save(link);

    assertTrue(repository.findByCode("abc1234").isPresent());
  }

  @Test
  void shouldReturnEmptyForUnknownCode() {

    assertTrue(repository.findByCode("unknown").isEmpty());
  }

  @Test
  void shouldFindLinksByOwner() {

    UUID ownerId = UUID.randomUUID();
    UUID anotherOwnerId = UUID.randomUUID();

    repository.save(createLink("link001", ownerId, Instant.now().plusSeconds(3600)));

    repository.save(createLink("link002", ownerId, Instant.now().plusSeconds(3600)));

    repository.save(createLink("link003", anotherOwnerId, Instant.now().plusSeconds(3600)));

    assertEquals(2, repository.findAllByOwner(ownerId).size());
  }

  @Test
  void shouldDeleteLinkByCode() {

    ShortLink link = createLink("delete1", UUID.randomUUID(), Instant.now().plusSeconds(3600));

    repository.save(link);

    assertTrue(repository.deleteByCode("delete1"));

    assertTrue(repository.findByCode("delete1").isEmpty());
  }

  @Test
  void shouldDeleteOnlyExpiredLinks() {

    UUID ownerId = UUID.randomUUID();

    ShortLink expiredLink = createLink("expired1", ownerId, Instant.now().minusSeconds(3600));

    ShortLink activeLink = createLink("active01", ownerId, Instant.now().plusSeconds(3600));

    repository.save(expiredLink);
    repository.save(activeLink);

    int deleted = repository.deleteExpiredLinks();

    assertEquals(1, deleted);

    assertTrue(repository.findByCode("expired1").isEmpty());

    assertTrue(repository.findByCode("active01").isPresent());
  }

  @Test
  void shouldReturnZeroWhenNoExpiredLinksExist() {

    ShortLink link = createLink("active02", UUID.randomUUID(), Instant.now().plusSeconds(3600));

    repository.save(link);

    assertEquals(0, repository.deleteExpiredLinks());
  }
}
