package org.example.urlshortener.model;

import java.time.Instant;
import java.util.UUID;

public class ShortLink {

  private final String shortCode;
  private final String originalUrl;
  private final UUID ownerId;
  private final Instant createdAt;
  private final Instant expiresAt;

  private int maxClicks;
  private int clickCount;

  public ShortLink(
      String shortCode,
      String originalUrl,
      UUID ownerId,
      Instant createdAt,
      Instant expiresAt,
      int maxClicks) {

    if (shortCode == null || shortCode.isBlank()) {
      throw new IllegalArgumentException("Короткий код не может быть пустым");
    }

    if (originalUrl == null || originalUrl.isBlank()) {
      throw new IllegalArgumentException("Исходная ссылка не может быть пустой");
    }

    if (ownerId == null) {
      throw new IllegalArgumentException("Владелец ссылки не указан");
    }

    if (createdAt == null || expiresAt == null || !expiresAt.isAfter(createdAt)) {
      throw new IllegalArgumentException("Некорректный срок действия ссылки");
    }

    if (maxClicks <= 0) {
      throw new IllegalArgumentException("Лимит переходов должен быть больше нуля");
    }

    this.shortCode = shortCode.trim();
    this.originalUrl = originalUrl.trim();
    this.ownerId = ownerId;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.maxClicks = maxClicks;
    this.clickCount = 0;
  }

  public String getShortCode() {
    return shortCode;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public synchronized int getMaxClicks() {
    return maxClicks;
  }

  public synchronized int getClickCount() {
    return clickCount;
  }

  public synchronized int getRemainingClicks() {
    return Math.max(0, maxClicks - clickCount);
  }

  public boolean isExpired() {
    return !Instant.now().isBefore(expiresAt);
  }

  public synchronized boolean isClickLimitReached() {
    return clickCount >= maxClicks;
  }

  public synchronized boolean isAvailable() {
    return !isExpired() && !isClickLimitReached();
  }

  public synchronized void registerClick() {

    if (isExpired()) {
      throw new IllegalStateException("Срок действия ссылки истёк");
    }

    if (isClickLimitReached()) {
      throw new IllegalStateException("Лимит переходов исчерпан");
    }

    clickCount++;
  }

  public synchronized void updateMaxClicks(int newMaxClicks) {

    if (newMaxClicks <= 0) {
      throw new IllegalArgumentException("Лимит должен быть больше нуля");
    }

    if (newMaxClicks < clickCount) {
      throw new IllegalArgumentException(
          "Новый лимит не может быть меньше " + "уже выполненных переходов");
    }

    this.maxClicks = newMaxClicks;
  }
}
