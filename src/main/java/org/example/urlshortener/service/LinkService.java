package org.example.urlshortener.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.example.urlshortener.config.AppConfig;
import org.example.urlshortener.model.ShortLink;
import org.example.urlshortener.model.User;
import org.example.urlshortener.repository.LinkRepository;
import org.example.urlshortener.util.CodeGenerator;
import org.example.urlshortener.util.UrlValidator;

public class LinkService {

  private final LinkRepository linkRepository;
  private final CodeGenerator codeGenerator;

  public LinkService(LinkRepository linkRepository, CodeGenerator codeGenerator) {
    this.linkRepository = linkRepository;
    this.codeGenerator = codeGenerator;
  }

  public ShortLink createLink(String originalUrl, User owner, int maxClicks) {

    if (!UrlValidator.isValid(originalUrl)) {
      throw new IllegalArgumentException(
          "Введите корректную ссылку, начинающуюся с http:// или https://");
    }

    if (maxClicks <= 0) {
      throw new IllegalArgumentException("Лимит переходов должен быть больше нуля");
    }

    String code;

    do {
      code = codeGenerator.generate(AppConfig.SHORT_CODE_LENGTH);

    } while (linkRepository.findByCode(code).isPresent());

    Instant now = Instant.now();

    ShortLink link =
        new ShortLink(
            code,
            originalUrl.trim(),
            owner.getId(),
            now,
            now.plus(AppConfig.LINK_TTL_HOURS, ChronoUnit.HOURS),
            maxClicks);

    return linkRepository.save(link);
  }

  public String openLink(String code) {

    ShortLink link =
        linkRepository
            .findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Короткая ссылка не найдена"));

    if (link.isExpired()) {
      linkRepository.deleteByCode(code);

      throw new IllegalStateException("Срок действия ссылки истёк");
    }

    if (link.isClickLimitReached()) {
      throw new IllegalStateException("Лимит переходов по ссылке исчерпан");
    }

    link.registerClick();

    return link.getOriginalUrl();
  }

  public List<ShortLink> getLinksForUser(UUID ownerId) {

    return linkRepository.findAllByOwner(ownerId).stream()
        .sorted(Comparator.comparing(ShortLink::getCreatedAt).reversed())
        .toList();
  }

  public void updateClickLimit(String code, User owner, int newLimit) {

    ShortLink link = getOwnedLink(code, owner);

    if (newLimit < link.getClickCount()) {
      throw new IllegalArgumentException(
          "Новый лимит не может быть меньше уже выполненных переходов");
    }

    link.updateMaxClicks(newLimit);
  }

  public void deleteLink(String code, User owner) {

    getOwnedLink(code, owner);

    linkRepository.deleteByCode(code);
  }

  public int cleanupExpiredLinks() {
    return linkRepository.deleteExpiredLinks();
  }

  private ShortLink getOwnedLink(String code, User owner) {

    ShortLink link =
        linkRepository
            .findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Короткая ссылка не найдена"));

    if (!link.getOwnerId().equals(owner.getId())) {
      throw new SecurityException("Вы можете изменять и удалять только свои ссылки");
    }

    return link;
  }
}
