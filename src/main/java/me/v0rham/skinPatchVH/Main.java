package me.v0rham.skinPatchVH;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Точка входа для Paper и Spigot. Для Velocity см. me.v0rham.skinPatchVH.velocity.VelocityMain.
 * На Paper используется PlayerProfile API, на Spigot - режим совместимости через рефлексию.
 */
public final class Main extends JavaPlugin {

    private static final long UPDATE_CHECK_PERIOD_TICKS = 20L * 60 * 60 * 12;

    @Override
    public void onEnable() {
        SkinLogger logger = new SkinLogger() {
            @Override
            public void info(String message) {
                getLogger().info(message);
            }

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
        SkinSettings settings = SkinSettings.load(getDataFolder().toPath(), logger);

        final boolean paper = isPaper();
        SkinApplier applier;
        if (paper) {
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

        if (PluginInfo.BSTATS_ID_BUKKIT > 0) {
            Metrics metrics = new Metrics(this, PluginInfo.BSTATS_ID_BUKKIT);
            metrics.addCustomChart(new SimplePie("platform", () -> paper ? "Paper" : "Spigot"));
        }

        String version = currentVersion();
        if (settings.updateChecker() && PluginInfo.SPIGOT_RESOURCE_ID > 0 && version != null) {
            UpdateChecker checker = new UpdateChecker(version, logger);
            getServer().getPluginManager().registerEvents(new UpdateNotifyListener(checker), this);
            getServer().getScheduler().runTaskTimerAsynchronously(
                    this, checker::check, 0L, UPDATE_CHECK_PERIOD_TICKS);
        }
    }

    private String currentVersion() {
        try {
            return getDescription().getVersion();
        } catch (Throwable t) {
            // метод может отсутствовать в очень новых версиях API
            return null;
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
