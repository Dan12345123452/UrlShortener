package org.example.urlshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class AppConfig {

  private static final String CONFIG_FILE = "application.properties";

  private static final Properties PROPERTIES = loadProperties();

  // Время жизни ссылки
  public static final int LINK_TTL_HOURS = readPositiveInt("link.ttl.hours");

  // Лимит переходов по умолчанию
  public static final int DEFAULT_MAX_CLICKS = readPositiveInt("link.default.max-clicks");

  // Длина короткого кода
  public static final int SHORT_CODE_LENGTH = readPositiveInt("link.short-code.length");

  // Интервал очистки просроченных ссылок
  public static final int CLEANUP_INTERVAL_SECONDS =
      readPositiveInt("link.cleanup.interval.seconds");

  private AppConfig() {}

  private static Properties loadProperties() {

    Properties properties = new Properties();

    try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {

      if (input == null) {
        throw new IllegalStateException("Не найден файл конфигурации: " + CONFIG_FILE);
      }

      properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));

      return properties;

    } catch (IOException e) {

      throw new IllegalStateException("Не удалось загрузить конфигурацию", e);
    }
  }

  private static int readPositiveInt(String key) {

    String value = PROPERTIES.getProperty(key);

    if (value == null || value.isBlank()) {
      throw new IllegalStateException("В конфигурации отсутствует параметр: " + key);
    }

    int number;

    try {
      number = Integer.parseInt(value.trim());

    } catch (NumberFormatException e) {

      throw new IllegalStateException("Параметр " + key + " должен быть целым числом", e);
    }

    if (number <= 0) {
      throw new IllegalStateException("Параметр " + key + " должен быть больше нуля");
    }

    return number;
  }
}
