package org.example.urlshortener.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.example.urlshortener.model.ShortLink;

public class LinkRepository {

  private final ConcurrentMap<String, ShortLink> links = new ConcurrentHashMap<>();

  public ShortLink save(ShortLink link) {
    links.put(link.getShortCode(), link);
    return link;
  }

  public Optional<ShortLink> findByCode(String code) {
    return Optional.ofNullable(links.get(code));
  }

  public List<ShortLink> findAllByOwner(UUID ownerId) {
    return links.values().stream().filter(link -> link.getOwnerId().equals(ownerId)).toList();
  }

  public boolean deleteByCode(String code) {
    return links.remove(code) != null;
  }

  public int deleteExpiredLinks() {
    int before = links.size();

    links.entrySet().removeIf(entry -> entry.getValue().isExpired());

    return before - links.size();
  }
}
