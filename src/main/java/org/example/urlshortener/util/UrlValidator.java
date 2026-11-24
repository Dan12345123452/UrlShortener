package org.example.urlshortener.util;

import java.net.URI;
import java.net.URISyntaxException;

public final class UrlValidator {

  private UrlValidator() {}

  public static boolean isValid(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }

    try {
      URI uri = new URI(value.trim());

      String scheme = uri.getScheme();

      return uri.isAbsolute()
          && scheme != null
          && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
          && uri.getHost() != null;

    } catch (URISyntaxException e) {
      return false;
    }
  }
}
