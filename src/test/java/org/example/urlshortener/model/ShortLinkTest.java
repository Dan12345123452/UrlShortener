package org.example.urlshortener.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShortLinkTest {

  private ShortLink createLink(int maxClicks) {

    Instant createdAt = Instant.now();

    return new ShortLink(
        "abc1234",
        "https://example.com",
        UUID.randomUUID(),
        createdAt,
        createdAt.plusSeconds(3600),
        maxClicks);
  }

  @Test
  void shouldReturnRemainingClicks() {

    ShortLink link = createLink(5);

    assertEquals(5, link.getRemainingClicks());
  }

  @Test
  void shouldRegisterClick() {

    ShortLink link = createLink(5);

    link.registerClick();

    assertEquals(1, link.getClickCount());
    assertEquals(4, link.getRemainingClicks());
  }

  @Test
  void shouldNotAllowClicksAboveLimit() {

    ShortLink link = createLink(1);

    link.registerClick();

    assertThrows(IllegalStateException.class, link::registerClick);

    assertEquals(1, link.getClickCount());
  }

  @Test
  void shouldDetectExpiredLink() {

    Instant now = Instant.now();

    ShortLink link =
        new ShortLink(
            "expired1",
            "https://example.com",
            UUID.randomUUID(),
            now.minusSeconds(7200),
            now.minusSeconds(3600),
            5);

    assertTrue(link.isExpired());
  }

  @Test
  void shouldNotBeAvailableWhenExpired() {

    Instant now = Instant.now();

    ShortLink link =
        new ShortLink(
            "expired2",
            "https://example.com",
            UUID.randomUUID(),
            now.minusSeconds(7200),
            now.minusSeconds(3600),
            5);

    assertFalse(link.isAvailable());
  }

  @Test
  void shouldNotBeAvailableWhenClickLimitReached() {

    ShortLink link = createLink(1);

    link.registerClick();

    assertFalse(link.isAvailable());
  }

  @Test
  void shouldAllowIncreasingClickLimit() {

    ShortLink link = createLink(1);

    link.registerClick();
    link.updateMaxClicks(3);

    assertEquals(3, link.getMaxClicks());
    assertEquals(2, link.getRemainingClicks());
  }

  @Test
  void shouldRejectLimitBelowClickCount() {

    ShortLink link = createLink(3);

    link.registerClick();
    link.registerClick();

    assertThrows(IllegalArgumentException.class, () -> link.updateMaxClicks(1));

    assertEquals(3, link.getMaxClicks());
  }

  @Test
  void shouldTrimShortCodeAndOriginalUrl() {

    Instant now = Instant.now();

    ShortLink link =
        new ShortLink(
            "  abc1234  ",
            "  https://example.com  ",
            UUID.randomUUID(),
            now,
            now.plusSeconds(3600),
            5);

    assertEquals("abc1234", link.getShortCode());
    assertEquals("https://example.com", link.getOriginalUrl());
  }

  @Test
  void shouldRejectBlankShortCode() {

    Instant now = Instant.now();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ShortLink(
                " ", "https://example.com", UUID.randomUUID(), now, now.plusSeconds(3600), 5));
  }
}
