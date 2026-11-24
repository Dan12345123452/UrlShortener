package org.example.urlshortener.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UrlValidatorTest {

  @Test
  void shouldAcceptHttpUrl() {

    assertTrue(UrlValidator.isValid("http://example.com"));
  }

  @Test
  void shouldAcceptHttpsUrl() {

    assertTrue(UrlValidator.isValid("https://example.com"));
  }

  @Test
  void shouldRejectNullUrl() {

    assertFalse(UrlValidator.isValid(null));
  }

  @Test
  void shouldRejectBlankUrl() {

    assertFalse(UrlValidator.isValid("   "));
  }

  @Test
  void shouldRejectUnsupportedProtocol() {

    assertFalse(UrlValidator.isValid("ftp://example.com"));
  }

  @Test
  void shouldRejectUrlWithoutHost() {

    assertFalse(UrlValidator.isValid("https://"));
  }
}
