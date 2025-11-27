package ru.otus.hw.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdMappingCache {

    private final Map<Long, String> authorIdMap = new ConcurrentHashMap<>();

    private final Map<Long, String> genreIdMap = new ConcurrentHashMap<>();

    private final Map<Long, String> bookIdMap = new ConcurrentHashMap<>();

    public void addAuthorMapItem(Long relationalId, String mongoId) {
        authorIdMap.put(relationalId, mongoId);
    }

    public void addGenreMapItem(Long relationalId, String mongoId) {
        genreIdMap.put(relationalId, mongoId);
    }

    public void addBookMapItem(Long relationalId, String mongoId) {
        bookIdMap.put(relationalId, mongoId);
    }

    public String getAuthorMongoId(Long relationalId) {
        return authorIdMap.get(relationalId);
    }

    public String getGenreMongoId(Long relationalId) {
        return genreIdMap.get(relationalId);
    }

    public String getBookMongoId(Long relationalId) {
        return bookIdMap.get(relationalId);
    }

    public void clear() {
        authorIdMap.clear();
        genreIdMap.clear();
        bookIdMap.clear();
    }
}
