package me.v0rham.skinPatchVH;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Проверка новой версии через API SpigotMC. Метод check блокирующий -
 * вызывать только из асинхронного потока.
 */
public final class UpdateChecker {

    private static final String API_URL = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static final String RESOURCE_URL = "https://www.spigotmc.org/resources/";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String currentVersion;
    private final SkinLogger logger;

    private volatile String newerVersion;
    private volatile String lastAnnounced;

    public UpdateChecker(String currentVersion, SkinLogger logger) {
        this.currentVersion = currentVersion;
        this.logger = logger;
    }

    public void check() {
        try {
            HttpRequest request = HttpRequest.newBuilder(
                            URI.create(API_URL + PluginInfo.SPIGOT_RESOURCE_ID))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "SkinPatchVH-UpdateChecker")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.warn("Не удалось проверить обновления: HTTP " + response.statusCode());
                return;
            }

            String latest = response.body().trim();
            if (latest.isEmpty() || latest.length() > 32) {
                return;
            }

            if (isNewer(latest, currentVersion)) {
                newerVersion = latest;
                if (!latest.equals(lastAnnounced)) {
                    lastAnnounced = latest;
                    logger.info(message(latest));
                }
            } else {
                newerVersion = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("Не удалось проверить обновления: " + e.getMessage());
        }
    }

    /** Версия новее текущей, если она найдена. */
    public Optional<String> getNewerVersion() {
        return Optional.ofNullable(newerVersion);
    }

    public String message(String latest) {
        return "Доступна новая версия SkinPatchVH: " + latest + " (установлена " + currentVersion + "). "
                + RESOURCE_URL + PluginInfo.SPIGOT_RESOURCE_ID + "/";
    }

    static boolean isNewer(String latest, String current) {
        List<Integer> a = parse(latest);
        List<Integer> b = parse(current);
        int size = Math.max(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            int x = i < a.size() ? a.get(i) : 0;
            int y = i < b.size() ? b.get(i) : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }

    private static List<Integer> parse(String version) {
        List<Integer> parts = new ArrayList<>();
        for (String piece : version.split("[^0-9]+")) {
            if (!piece.isEmpty()) {
                try {
                    parts.add(Integer.parseInt(piece));
                } catch (NumberFormatException ignored) {
                    // слишком длинное число - пропускаем
                }
            }
        }
        return parts;
    }
}
