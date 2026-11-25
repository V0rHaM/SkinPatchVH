package me.v0rham.skinPatchVH;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spigot: восстановление скина при входе. Скин скачивается асинхронно на этапе
 * AsyncPlayerPreLoginEvent, а в профиль записывается в PlayerLoginEvent -
 * до того, как игрок будет показан остальным.
 */
public final class SpigotSkinListener implements Listener {

    private final SkinManager manager;
    private final SpigotProfileEditor editor;
    private final Map<UUID, SkinData> pending = new ConcurrentHashMap<>();

    public SpigotSkinListener(SkinManager manager, SpigotProfileEditor editor) {
        this.manager = manager;
        this.editor = editor;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        // На online-серверах текстуры от Mojang уже есть, нужен только сохранённый выбор
        boolean profileHasTextures = Bukkit.getOnlineMode();
        manager.resolveForLogin(event.getUniqueId(), event.getName(), profileHasTextures)
                .ifPresent(data -> pending.put(event.getUniqueId(), data));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        SkinData data = pending.remove(player.getUniqueId());
        if (data == null || event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        // Если текстуры уже пришли (например, от прокси) и своего выбора нет - не трогаем
        if (editor.hasTextures(player) && !manager.hasStoredSkin(player.getUniqueId())) {
            return;
        }
        editor.set(player, data);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
