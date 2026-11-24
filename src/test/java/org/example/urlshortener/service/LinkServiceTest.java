package org.example.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import org.example.urlshortener.config.AppConfig;
import org.example.urlshortener.model.ShortLink;
import org.example.urlshortener.model.User;
import org.example.urlshortener.repository.LinkRepository;
import org.example.urlshortener.util.CodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkServiceTest {

  private LinkRepository repository;
  private LinkService linkService;

  private User owner;
  private User anotherUser;

  @BeforeEach
  void setUp() {

    repository = new LinkRepository();

    linkService = new LinkService(repository, new CodeGenerator());

    owner = new User("Alex");
    anotherUser = new User("Max");
  }

  @Test
  void shouldCreateShortLink() {

    ShortLink link = linkService.createLink("https://example.com", owner, 5);

    assertNotNull(link);
    assertEquals("https://example.com", link.getOriginalUrl());
    assertEquals(owner.getId(), link.getOwnerId());
    assertEquals(5, link.getMaxClicks());
    assertEquals(AppConfig.SHORT_CODE_LENGTH, link.getShortCode().length());
  }

  @Test
  void shouldRejectInvalidUrl() {

    assertThrows(
        IllegalArgumentException.class, () -> linkService.createLink("not a url", owner, 5));
  }

  @Test
  void shouldRejectNonPositiveClickLimit() {

    assertThrows(
        IllegalArgumentException.class,
        () -> linkService.createLink("https://example.com", owner, 0));
  }

  @Test
  void shouldOpenLinkAndRegisterClick() {

    ShortLink link = linkService.createLink("https://example.com", owner, 5);

    String result = linkService.openLink(link.getShortCode());

    assertEquals("https://example.com", result);
    assertEquals(1, link.getClickCount());
  }

  @Test
  void shouldBlockOpeningAfterClickLimit() {

    ShortLink link = linkService.createLink("https://example.com", owner, 1);

    linkService.openLink(link.getShortCode());

    assertThrows(IllegalStateException.class, () -> linkService.openLink(link.getShortCode()));

    assertEquals(1, link.getClickCount());
  }

  @Test
  void shouldRejectUnknownShortCode() {

    assertThrows(IllegalArgumentException.class, () -> linkService.openLink("unknown"));
  }

  @Test
  void shouldReturnOnlyLinksOfRequestedUser() {

    ShortLink firstLink = linkService.createLink("https://example.com", owner, 5);

    linkService.createLink("https://example.com", anotherUser, 5);

    var links = linkService.getLinksForUser(owner.getId());

    assertEquals(1, links.size());
    assertEquals(firstLink.getShortCode(), links.get(0).getShortCode());
  }

  @Test
  void shouldAllowOwnerToUpdateClickLimit() {

    ShortLink link = linkService.createLink("https://example.com", owner, 5);

    linkService.updateClickLimit(link.getShortCode(), owner, 10);

    assertEquals(10, link.getMaxClicks());
  }

  @Test
  void shouldRejectUpdatingAnotherUsersLink() {

    ShortLink link = linkService.createLink("https://example.com", owner, 5);

    assertThrows(
        SecurityException.class,
        () -> linkService.updateClickLimit(link.getShortCode(), anotherUser, 10));

    assertEquals(5, link.getMaxClicks());
  }

  @Test
  void shouldAllowOwnerToDeleteLink() {

    ShortLink link = linkService.createLink("https://example.com", owner, 5);

    linkService.deleteLink(link.getShortCode(), owner);

    assertTrue(repository.findByCode(link.getShortCode()).isEmpty());
  }

  @Test
  void shouldRejectDeletingAnotherUsersLink() {

    ShortLink link = linkService.createLink("https://example.com", owner, 5);

    assertThrows(
        SecurityException.class, () -> linkService.deleteLink(link.getShortCode(), anotherUser));

    assertTrue(repository.findByCode(link.getShortCode()).isPresent());
  }

  @Test
  void shouldCleanupExpiredLinks() {

    java.time.Instant now = java.time.Instant.now();

    ShortLink expiredLink =
        new ShortLink(
            "expired01",
            "https://example.com",
            owner.getId(),
            now.minusSeconds(7200),
            now.minusSeconds(3600),
            5);

    ShortLink activeLink =
        new ShortLink(
            "active01", "https://example.com", owner.getId(), now, now.plusSeconds(3600), 5);

    repository.save(expiredLink);
    repository.save(activeLink);

    int deleted = linkService.cleanupExpiredLinks();

    assertEquals(1, deleted);

    assertTrue(repository.findByCode("expired01").isEmpty());

    assertTrue(repository.findByCode("active01").isPresent());
  }

  @Test
  void shouldDeleteExpiredLinkWhenOpened() {

    java.time.Instant now = java.time.Instant.now();

    ShortLink expiredLink =
        new ShortLink(
            "expired02",
            "https://example.com",
            owner.getId(),
            now.minusSeconds(7200),
            now.minusSeconds(3600),
            5);

    repository.save(expiredLink);

    assertThrows(IllegalStateException.class, () -> linkService.openLink("expired02"));

    assertTrue(repository.findByCode("expired02").isEmpty());
  }
}
