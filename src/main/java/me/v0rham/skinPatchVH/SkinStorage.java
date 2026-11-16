package me.v0rham.skinPatchVH;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Хранилище выбранных игроками скинов (skins.json в папке плагина).
 * Не зависит от платформы, поэтому работает и на Paper, и на Velocity.
 */
public final class SkinStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;
    private final SkinLogger logger;
    private final Map<String, SkinData> skins = new HashMap<>();

    public SkinStorage(Path dataDirectory, SkinLogger logger) {
        this.logger = logger;
        this.file = dataDirectory.resolve("skins.json");
        load();
    }

    public synchronized Optional<SkinData> get(UUID id) {
        return Optional.ofNullable(skins.get(id.toString()));
    }

    public synchronized void set(UUID id, SkinData data) {
        skins.put(id.toString(), data);
        save();
    }

    public synchronized void remove(UUID id) {
        if (skins.remove(id.toString()) != null) {
            save();
        }
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                return;
            }
            for (Map.Entry<String, JsonElement> entry : parsed.getAsJsonObject().entrySet()) {
                JsonObject o = entry.getValue().getAsJsonObject();
                if (!o.has("value")) {
                    continue;
                }
                skins.put(entry.getKey(), new SkinData(
                        o.has("name") ? o.get("name").getAsString() : "unknown",
                        o.get("value").getAsString(),
                        o.has("signature") ? o.get("signature").getAsString() : null
                ));
            }
        } catch (Exception e) {
            logger.error("Не удалось прочитать skins.json", e);
        }
    }

    private void save() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, SkinData> entry : skins.entrySet()) {
            SkinData data = entry.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("name", data.name());
            o.addProperty("value", data.value());
            if (data.signature() != null) {
                o.addProperty("signature", data.signature());
            }
            root.add(entry.getKey(), o);
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Не удалось сохранить skins.json", e);
        }
    }
}
