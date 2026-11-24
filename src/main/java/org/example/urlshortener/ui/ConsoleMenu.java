package org.example.urlshortener.ui;

import java.awt.Desktop;
import java.net.URI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import org.example.urlshortener.config.AppConfig;
import org.example.urlshortener.model.ShortLink;
import org.example.urlshortener.model.User;
import org.example.urlshortener.service.LinkService;
import org.example.urlshortener.service.UserService;

public class ConsoleMenu {

  private final Scanner scanner;
  private final LinkService linkService;
  private final UserService userService;

  private User currentUser;

  public ConsoleMenu(
      Scanner scanner, LinkService linkService, UserService userService, User currentUser) {
    this.scanner = scanner;
    this.linkService = linkService;
    this.userService = userService;
    this.currentUser = currentUser;
  }

  public void run() {

    boolean running = true;

    while (running) {

      printMenu();

      System.out.print("Выберите действие: ");

      String command = scanner.nextLine().trim();

      try {
        switch (command) {
          case "1" -> createLink();

          case "2" -> openLink();

          case "3" -> showMyLinks();

          case "4" -> changeClickLimit();

          case "5" -> deleteLink();

          case "6" -> switchUser();

          case "0" -> running = false;

          default -> System.out.println("Неизвестная команда. Выберите пункт от 0 до 6.");
        }

      } catch (RuntimeException e) {
        System.out.println("Ошибка: " + e.getMessage());
      }

      System.out.println();
    }

    System.out.println("Работа программы завершена.");
  }

  private void printMenu() {

    System.out.println("========================================");
    System.out.println("СЕРВИС СОКРАЩЕНИЯ ССЫЛОК");

    System.out.println("Пользователь: " + currentUser.getUsername());

    System.out.println("1. Создать короткую ссылку");
    System.out.println("2. Открыть ссылку по короткому коду");
    System.out.println("3. Показать мои ссылки и статистику");
    System.out.println("4. Изменить лимит переходов");
    System.out.println("5. Удалить мою ссылку");
    System.out.println("6. Сменить пользователя");
    System.out.println("0. Выход");
  }

  private void createLink() {

    System.out.print("Введите исходную ссылку (http:// или https://): ");

    String url = scanner.nextLine().trim();

    int maxClicks =
        readPositiveInt(
            "Лимит переходов (Enter — " + AppConfig.DEFAULT_MAX_CLICKS + "): ",
            AppConfig.DEFAULT_MAX_CLICKS);

    ShortLink link = linkService.createLink(url, currentUser, maxClicks);

    System.out.println("Ссылка создана успешно!");

    System.out.println("Короткий код: " + link.getShortCode());

    System.out.println("Отображаемый адрес: short.local/" + link.getShortCode());

    System.out.println("Срок действия до: " + formatDate(link));

    System.out.println(
        "Важно: это учебная консольная версия. " + "Для открытия выберите пункт 2 и введите код.");
  }

  private void openLink() {

    System.out.print("Введите короткий код: ");

    String code = scanner.nextLine().trim();

    String originalUrl = linkService.openLink(code);

    System.out.println("Исходный адрес: " + originalUrl);

    try {

      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

        Desktop.getDesktop().browse(URI.create(originalUrl));

        System.out.println("Ссылка открыта в браузере.");

      } else {
        System.out.println(
            "Автоматическое открытие браузера недоступно. " + "Скопируйте адрес выше.");
      }

    } catch (Exception e) {

      System.out.println("Не удалось открыть браузер. Скопируйте адрес выше.");
    }
  }

  private void showMyLinks() {

    List<ShortLink> links = linkService.getLinksForUser(currentUser.getId());

    if (links.isEmpty()) {
      System.out.println("У вас пока нет ссылок.");
      return;
    }

    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    for (ShortLink link : links) {

      System.out.println("----------------------------------------");

      System.out.println("Код: " + link.getShortCode());

      System.out.println("Исходная ссылка: " + link.getOriginalUrl());

      System.out.println("Переходы: " + link.getClickCount() + "/" + link.getMaxClicks());

      System.out.println("Осталось переходов: " + link.getRemainingClicks());

      System.out.println("Действует до: " + formatter.format(link.getExpiresAt()));

      if (link.isExpired()) {

        System.out.println("Статус: срок действия истёк");

      } else if (link.isClickLimitReached()) {

        System.out.println("Статус: лимит переходов исчерпан");

      } else {

        System.out.println("Статус: активна");
      }
    }
  }

  private void changeClickLimit() {

    System.out.print("Введите код своей ссылки: ");

    String code = scanner.nextLine().trim();

    int newLimit = readPositiveInt("Введите новый лимит переходов: ", -1);

    linkService.updateClickLimit(code, currentUser, newLimit);

    System.out.println("Лимит переходов обновлён.");
  }

  private void deleteLink() {

    System.out.print("Введите код ссылки, которую нужно удалить: ");

    String code = scanner.nextLine().trim();

    linkService.deleteLink(code, currentUser);

    System.out.println("Ссылка удалена.");
  }

  private void switchUser() {

    System.out.print("Введите имя пользователя: ");

    String username = scanner.nextLine();

    currentUser = userService.loginOrRegister(username);

    System.out.println("Текущий пользователь: " + currentUser.getUsername());
  }

  private int readPositiveInt(String prompt, int defaultValue) {

    while (true) {

      System.out.print(prompt);

      String value = scanner.nextLine().trim();

      if (value.isEmpty() && defaultValue > 0) {
        return defaultValue;
      }

      try {

        int number = Integer.parseInt(value);

        if (number > 0) {
          return number;
        }

      } catch (NumberFormatException ignored) {
        // Запрашиваем значение повторно.
      }

      System.out.println("Введите целое число больше нуля.");
    }
  }

  private String formatDate(ShortLink link) {

    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(link.getExpiresAt());
  }
}
