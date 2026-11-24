package org.example.urlshortener;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.example.urlshortener.config.AppConfig;
import org.example.urlshortener.model.User;
import org.example.urlshortener.repository.LinkRepository;
import org.example.urlshortener.repository.UserRepository;
import org.example.urlshortener.service.LinkService;
import org.example.urlshortener.service.UserService;
import org.example.urlshortener.ui.ConsoleMenu;
import org.example.urlshortener.util.CodeGenerator;

public class Main {

  public static void main(String[] args) {

    Scanner scanner = new Scanner(System.in);

    UserRepository userRepository = new UserRepository(Path.of("data", "users.txt"));

    UserService userService = new UserService(userRepository);

    System.out.println("Добро пожаловать в сервис сокращения ссылок!");

    System.out.print("Введите имя пользователя: ");

    User currentUser = userService.loginOrRegister(scanner.nextLine());

    System.out.println("Вы вошли как: " + currentUser.getUsername());

    LinkRepository linkRepository = new LinkRepository();

    LinkService linkService = new LinkService(linkRepository, new CodeGenerator());

    ScheduledExecutorService cleanupExecutor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread thread = new Thread(r, "expired-links-cleanup");

              thread.setDaemon(true);

              return thread;
            });

    cleanupExecutor.scheduleAtFixedRate(
        () -> {
          try {
            int deleted = linkService.cleanupExpiredLinks();

            if (deleted > 0) {
              System.out.println(
                  "[Автоматическая очистка] " + "Удалено просроченных ссылок: " + deleted);
            }

          } catch (RuntimeException e) {
            System.err.println("[Ошибка очистки] " + e.getMessage());
          }
        },
        AppConfig.CLEANUP_INTERVAL_SECONDS,
        AppConfig.CLEANUP_INTERVAL_SECONDS,
        TimeUnit.SECONDS);

    try {

      ConsoleMenu menu = new ConsoleMenu(scanner, linkService, userService, currentUser);

      menu.run();

    } finally {

      cleanupExecutor.shutdownNow();

      scanner.close();
    }
  }
}
