package me.v0rham.skinPatchVH;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Получение скинов с серверов Mojang по нику. Метод fetch блокирующий,
 * вызывать его нужно только из асинхронного потока.
 */
public final class SkinService {

    private final Map<String, SkinData> cache = new ConcurrentHashMap<>();

    public Optional<SkinData> fetch(String nick) {
        String key = nick.toLowerCase(Locale.ROOT);
        SkinData cached = cache.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }

        PlayerProfile profile = Bukkit.createProfile(nick);
        // onlineMode = true, чтобы запрос к Mojang шёл и на offline-серверах
        if (!profile.complete(true, true)) {
            return Optional.empty();
        }

        for (ProfileProperty property : profile.getProperties()) {
            if ("textures".equals(property.getName())) {
                String name = profile.getName() != null ? profile.getName() : nick;
                SkinData data = new SkinData(name, property.getValue(), property.getSignature());
                cache.put(key, data);
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }
}
