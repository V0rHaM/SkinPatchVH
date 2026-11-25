package me.v0rham.skinPatchVH.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.v0rham.skinPatchVH.PluginInfo;
import me.v0rham.skinPatchVH.SkinLogger;
import me.v0rham.skinPatchVH.SkinManager;
import me.v0rham.skinPatchVH.SkinSettings;
import me.v0rham.skinPatchVH.UpdateChecker;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Точка входа для Velocity (описана в velocity-plugin.json).
 * Для Paper/Spigot см. me.v0rham.skinPatchVH.Main.
 */
public final class VelocityMain {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory,
                        Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        SkinLogger skinLogger = new SkinLogger() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message) {
                logger.warn(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                logger.error(message, throwable);
            }
        };

        SkinManager manager = new SkinManager(dataDirectory, skinLogger);
        SkinSettings settings = SkinSettings.load(dataDirectory, skinLogger);

        server.getEventManager().register(this, new VelocitySkinListener(manager));

        CommandMeta meta = server.getCommandManager().metaBuilder("skin").plugin(this).build();
        server.getCommandManager().register(meta, new VelocitySkinCommand(this, server, manager));

        if (PluginInfo.BSTATS_ID_VELOCITY > 0) {
            metricsFactory.make(this, PluginInfo.BSTATS_ID_VELOCITY);
        }

        String version = server.getPluginManager().fromInstance(this)
                .flatMap(container -> container.getDescription().getVersion())
                .orElse(null);
        if (settings.updateChecker() && PluginInfo.SPIGOT_RESOURCE_ID > 0 && version != null) {
            UpdateChecker checker = new UpdateChecker(version, skinLogger);
            server.getEventManager().register(this, new VelocityUpdateListener(checker));
            server.getScheduler().buildTask(this, checker::check)
                    .repeat(12, TimeUnit.HOURS)
                    .schedule();
        }
    }
}
