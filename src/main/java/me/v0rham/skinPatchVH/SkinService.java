package me.v0rham.skinPatchVH;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Получение скинов с серверов Mojang по нику напрямую через HTTP API
 * (работает и на online-, и на offline-серверах).
 * Методы блокирующие, вызывать только из асинхронного потока.
 */
public final class SkinService {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final Map<String, SkinData> cache = new ConcurrentHashMap<>();

    /**
     * @return скин, либо empty, если такого ника (или текстур) нет
     * @throws IOException если Mojang недоступен или вернул ошибку (например, лимит запросов)
     */
    public Optional<SkinData> fetch(String nick) throws IOException, InterruptedException {
        String key = nick.toLowerCase(Locale.ROOT);
        SkinData cached = cache.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }

        JsonObject account = get(UUID_URL + nick);
        if (account == null || !account.has("id")) {
            return Optional.empty();
        }
        String uuid = account.get("id").getAsString();
        String realName = account.has("name") ? account.get("name").getAsString() : nick;

        JsonObject profile = get(PROFILE_URL + uuid + "?unsigned=false");
        if (profile == null || !profile.has("properties")) {
            return Optional.empty();
        }

        JsonArray properties = profile.getAsJsonArray("properties");
        for (JsonElement element : properties) {
            JsonObject property = element.getAsJsonObject();
            if ("textures".equals(property.get("name").getAsString())) {
                String signature = property.has("signature") ? property.get("signature").getAsString() : null;
                SkinData data = new SkinData(realName, property.get("value").getAsString(), signature);
                cache.put(key, data);
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    private JsonObject get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int code = response.statusCode();
        if (code == 200) {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        }
        if (code == 204 || code == 404) {
            return null;
        }
        throw new IOException("HTTP " + code + " при запросе " + url);
    }
}
