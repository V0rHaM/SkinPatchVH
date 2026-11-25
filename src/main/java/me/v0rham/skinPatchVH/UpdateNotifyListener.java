package me.v0rham.skinPatchVH;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Paper/Spigot: сообщает администраторам о новой версии при входе.
 */
public final class UpdateNotifyListener implements Listener {

    private final UpdateChecker checker;

    public UpdateNotifyListener(UpdateChecker checker) {
        this.checker = checker;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(PluginInfo.UPDATE_PERMISSION)) {
            return;
        }
        checker.getNewerVersion().ifPresent(
                latest -> player.sendMessage(ChatColor.GOLD + checker.message(latest)));
    }
}
