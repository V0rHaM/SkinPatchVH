package me.v0rham.skinPatchVH.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.v0rham.skinPatchVH.PluginInfo;
import me.v0rham.skinPatchVH.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Velocity: сообщает администраторам о новой версии при входе.
 */
public final class VelocityUpdateListener {

    private final UpdateChecker checker;

    public VelocityUpdateListener(UpdateChecker checker) {
        this.checker = checker;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PluginInfo.UPDATE_PERMISSION)) {
            return;
        }
        checker.getNewerVersion().ifPresent(latest -> player.sendMessage(
                Component.text(checker.message(latest), NamedTextColor.GOLD)));
    }
}
