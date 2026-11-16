package me.v0rham.skinPatchVH;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Восстанавливает скин игрока при входе на сервер:
 * 1) если игрок ранее выбирал скин командой - ставим сохранённый;
 * 2) иначе, если у профиля нет текстур (offline-режим), берём скин по нику игрока.
 */
public final class SkinListener implements Listener {

    private final SkinStorage storage;
    private final SkinService service;
    private final Logger logger;

    public SkinListener(SkinStorage storage, SkinService service, Logger logger) {
        this.storage = storage;
        this.service = service;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        PlayerProfile profile = event.getPlayerProfile();
        SkinData data = storage.get(event.getUniqueId()).orElse(null);

        if (data == null && !hasTextures(profile)) {
            try {
                Optional<SkinData> fetched = service.fetch(event.getName());
                data = fetched.orElse(null);
            } catch (Exception e) {
                logger.warning("Не удалось получить скин для " + event.getName() + ": " + e.getMessage());
            }
        }

        if (data == null) {
            return;
        }

        profile.setProperty(new ProfileProperty("textures", data.value(), data.signature()));
        event.setPlayerProfile(profile);
    }

    private boolean hasTextures(PlayerProfile profile) {
        return profile.getProperties().stream().anyMatch(p -> "textures".equals(p.getName()));
    }
}
