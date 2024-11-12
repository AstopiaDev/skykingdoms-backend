package net.skykingdoms.backend.velocity.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;
import net.skykingdoms.backend.base.util.optionals.Optional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MinecraftAuthenticator {
    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private final HttpClient client;
    private final Cache<String, Profile> cache;
    private final RateLimiter rateLimiter;

    public MinecraftAuthenticator() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.cache = CacheBuilder.newBuilder().build();
        this.rateLimiter = RateLimiter.create(600);
    }

    public Optional<Profile> findProfile(String username) throws IOException, RateLimitException {
        Profile cached = cache.getIfPresent(username);
        if (cached != null) {
            return Optional.of(cached);
        }

        if (!username.matches("[a-zA-Z0-9_]{3,16}")) {
            return Optional.empty();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UUID_URL + username))
                .header("Accept", "application/json")
                .GET()
                .build();

        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException();
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204 || response.statusCode() == 404) {
                return Optional.empty();
            }

            if (response.statusCode() == 429) {
                throw new RateLimitException();
            }

            if (response.statusCode() != 200) {
                throw new IOException("Unexpected response code: " + response.statusCode());
            }

            Profile profile = this.parseProfile(response.body());
            if (profile != null) {
                cache.put(username, profile);
            }
            return Optional.of(profile);

        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    private Profile parseProfile(String json) {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return null;
        }

        try {
            String[] parts = json.substring(1, json.length() - 1).split(",");
            String id = null;
            String name = null;

            for (String part : parts) {
                part = part.trim();
                String[] keyValue = part.split(":");
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");

                if ("id".equals(key)) {
                    id = value;
                } else if ("name".equals(key)) {
                    name = value;
                }
            }

            if (id != null && name != null) {
                Profile profile = new Profile();
                profile.id = id;
                profile.name = name;
                return profile;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static class RateLimitException extends Exception {
        static final int RATE_LIMIT_RESPONSE_CODE = 429;
    }
}

