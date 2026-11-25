package me.v0rham.skinPatchVH;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Настройки плагина (config.json в папке плагина), общие для всех платформ.
 */
public final class SkinSettings {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final boolean updateChecker;

    private SkinSettings(boolean updateChecker) {
        this.updateChecker = updateChecker;
    }

    /** Проверять ли наличие новой версии на SpigotMC. */
    public boolean updateChecker() {
        return updateChecker;
    }

    public static SkinSettings load(Path dataDirectory, SkinLogger logger) {
        Path file = dataDirectory.resolve("config.json");
        boolean updateChecker = true;
        try {
            if (Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    JsonElement parsed = JsonParser.parseReader(reader);
                    if (parsed.isJsonObject() && parsed.getAsJsonObject().has("update-checker")) {
                        updateChecker = parsed.getAsJsonObject().get("update-checker").getAsBoolean();
                    }
                }
            } else {
                JsonObject defaults = new JsonObject();
                defaults.addProperty("update-checker", true);
                Files.createDirectories(dataDirectory);
                Files.writeString(file, GSON.toJson(defaults), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.warn("Не удалось прочитать config.json, используются значения по умолчанию: " + e);
        }
        return new SkinSettings(updateChecker);
    }
}
