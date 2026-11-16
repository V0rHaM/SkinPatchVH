package me.v0rham.skinPatchVH;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Paper: восстанавливает скин игрока при входе на сервер.
 */
public final class SkinListener implements Listener {

    private final SkinManager manager;

    public SkinListener(SkinManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        PlayerProfile profile = event.getPlayerProfile();
        boolean hasTextures = profile.getProperties().stream().anyMatch(p -> "textures".equals(p.getName()));

        manager.resolveForLogin(event.getUniqueId(), event.getName(), hasTextures).ifPresent(data -> {
            profile.setProperty(new ProfileProperty("textures", data.value(), data.signature()));
            event.setPlayerProfile(profile);
        });
    }
}
