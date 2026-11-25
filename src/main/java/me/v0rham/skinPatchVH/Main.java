package me.v0rham.skinPatchVH;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Точка входа для Paper и Spigot. Для Velocity см. me.v0rham.skinPatchVH.velocity.VelocityMain.
 * На Paper используется PlayerProfile API, на Spigot - режим совместимости через рефлексию.
 */
public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        SkinLogger logger = new SkinLogger() {
            @Override
            public void warn(String message) {
                getLogger().warning(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                getLogger().log(Level.SEVERE, message, throwable);
            }
        };
        SkinManager manager = new SkinManager(getDataFolder().toPath(), logger);

        SkinApplier applier;
        if (isPaper()) {
            getServer().getPluginManager().registerEvents(new SkinListener(manager), this);
            applier = new PaperSkinApplier();
        } else {
            getLogger().info("Paper не обнаружен, включён режим совместимости для Spigot.");
            SpigotProfileEditor editor = new SpigotProfileEditor(logger);
            getServer().getPluginManager().registerEvents(new SpigotSkinListener(manager, editor), this);
            applier = new SpigotSkinApplier(this, editor);
        }

        PluginCommand command = getCommand("skin");
        if (command != null) {
            command.setExecutor(new SkinCommand(this, manager, applier));
        }
    }

    private static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
