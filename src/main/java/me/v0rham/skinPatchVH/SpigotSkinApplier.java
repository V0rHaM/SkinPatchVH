package me.v0rham.skinPatchVH;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Spigot: меняет GameProfile через рефлексию и обновляет игрока для остальных
 * (скрыть/показать). Сам игрок увидит новый скин после перезахода.
 */
public final class SpigotSkinApplier implements SkinApplier {

    private final JavaPlugin plugin;
    private final SpigotProfileEditor editor;

    public SpigotSkinApplier(JavaPlugin plugin, SpigotProfileEditor editor) {
        this.plugin = plugin;
        this.editor = editor;
    }

    @Override
    public Result apply(Player player, SkinData data) {
        if (!editor.set(player, data)) {
            return Result.FAILED;
        }
        refreshForOthers(player);
        return Result.RELOG_REQUIRED;
    }

    private void refreshForOthers(Player target) {
        List<Player> viewers = new ArrayList<>(Bukkit.getOnlinePlayers());
        viewers.remove(target);
        for (Player viewer : viewers) {
            viewer.hidePlayer(plugin, target);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline()) {
                return;
            }
            for (Player viewer : viewers) {
                if (viewer.isOnline()) {
                    viewer.showPlayer(plugin, target);
                }
            }
        }, 2L);
    }
}
