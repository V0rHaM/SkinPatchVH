package me.v0rham.skinPatchVH;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Хранилище выбранных игроками скинов (skins.yml в папке плагина).
 */
public final class SkinStorage {

    private final File file;
    private final Logger logger;
    private final YamlConfiguration yaml;

    public SkinStorage(File dataFolder, Logger logger) {
        this.logger = logger;
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.warning("Не удалось создать папку плагина");
        }
        this.file = new File(dataFolder, "skins.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized Optional<SkinData> get(UUID id) {
        String path = "players." + id;
        String value = yaml.getString(path + ".value");
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(new SkinData(
                yaml.getString(path + ".name", "unknown"),
                value,
                yaml.getString(path + ".signature")
        ));
    }

    public synchronized void set(UUID id, SkinData data) {
        String path = "players." + id;
        yaml.set(path + ".name", data.name());
        yaml.set(path + ".value", data.value());
        yaml.set(path + ".signature", data.signature());
        save();
    }

    public synchronized void remove(UUID id) {
        yaml.set("players." + id, null);
        save();
    }

    private void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Не удалось сохранить skins.yml", e);
        }
    }
}
