package me.v0rham.skinPatchVH.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.v0rham.skinPatchVH.SkinLogger;
import me.v0rham.skinPatchVH.SkinManager;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Точка входа для Velocity (описана в velocity-plugin.json).
 * Для Paper см. me.v0rham.skinPatchVH.Main.
 */
public final class VelocityMain {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public VelocityMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        SkinManager manager = new SkinManager(dataDirectory, new SkinLogger() {
            @Override
            public void warn(String message) {
                logger.warn(message);
            }

            @Override
            public void error(String message, Throwable throwable) {
                logger.error(message, throwable);
            }
        });

        server.getEventManager().register(this, new VelocitySkinListener(manager));

        CommandMeta meta = server.getCommandManager().metaBuilder("skin").plugin(this).build();
        server.getCommandManager().register(meta, new VelocitySkinCommand(this, server, manager));
    }
}
