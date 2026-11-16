package me.v0rham.skinPatchVH;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Точка входа для Paper. Для Velocity см. me.v0rham.skinPatchVH.velocity.VelocityMain.
 */
public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        SkinManager manager = new SkinManager(getDataFolder().toPath(), new SkinLogger() {
            @Override
            public void warn(String message) {
                getLogger().warning(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                getLogger().log(Level.SEVERE, message, throwable);
            }
        });

        getServer().getPluginManager().registerEvents(new SkinListener(manager), this);

        PluginCommand command = getCommand("skin");
        if (command != null) {
            command.setExecutor(new SkinCommand(this, manager));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
