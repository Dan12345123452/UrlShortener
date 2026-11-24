package org.example.urlshortener.util;

import java.security.SecureRandom;

public class CodeGenerator {

  private static final String CHARACTERS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private final SecureRandom random = new SecureRandom();

  public String generate(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("Длина короткого кода должна быть больше нуля");
    }

    StringBuilder code = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
    }

    return code.toString();
  }
}
