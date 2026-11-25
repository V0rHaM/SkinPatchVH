package me.v0rham.skinPatchVH;

import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Режим совместимости для Spigot/CraftBukkit: правит свойства GameProfile игрока
 * через рефлексию (без привязки к версии NMS). Использует только классы,
 * которые есть на сервере во время работы, поэтому не требует authlib при компиляции.
 * <p>
 * Ограничение: на версиях, где PropertyMap неизменяемый, правка невозможна -
 * тогда метод вернёт false и один раз напишет предупреждение в лог.
 */
public final class SpigotProfileEditor {

    private final SkinLogger logger;
    private boolean warned;

    public SpigotProfileEditor(SkinLogger logger) {
        this.logger = logger;
    }

    public boolean hasTextures(Player player) {
        try {
            return properties(player).containsKey("textures");
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @param data скин или null, чтобы убрать текстуры
     * @return true, если профиль удалось изменить
     */
    public boolean set(Player player, @Nullable SkinData data) {
        try {
            Multimap<String, Object> properties = properties(player);
            properties.removeAll("textures");
            if (data != null) {
                properties.put("textures", newProperty(data));
            }
            return true;
        } catch (Throwable t) {
            if (!warned) {
                warned = true;
                logger.warn("Не удалось изменить профиль игрока в режиме совместимости Spigot ("
                        + t.getClass().getSimpleName() + "). Для полной поддержки используйте Paper "
                        + "или установите плагин на прокси Velocity.");
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Multimap<String, Object> properties(Player player) throws ReflectiveOperationException {
        Object profile = gameProfile(player);
        Method getter = find(profile.getClass(), "getProperties", "properties");
        return (Multimap<String, Object>) getter.invoke(profile);
    }

    private Object gameProfile(Player player) throws ReflectiveOperationException {
        try {
            return player.getClass().getMethod("getProfile").invoke(player);
        } catch (NoSuchMethodException e) {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            return handle.getClass().getMethod("getGameProfile").invoke(handle);
        }
    }

    private Object newProperty(SkinData data) throws ReflectiveOperationException {
        Class<?> type = Class.forName("com.mojang.authlib.properties.Property");
        return type.getConstructor(String.class, String.class, String.class)
                .newInstance("textures", data.value(), data.signature());
    }

    private Method find(Class<?> type, String... names) throws NoSuchMethodException {
        for (String name : names) {
            try {
                return type.getMethod(name);
            } catch (NoSuchMethodException ignored) {
                // пробуем следующее имя
            }
        }
        throw new NoSuchMethodException(type.getName() + "#" + names[0]);
    }
}
