package me.v0rham.skinPatchVH;

/**
 * Идентификаторы внешних сервисов. Значение 0 отключает соответствующую функцию.
 */
public final class PluginInfo {

    /** ID ресурса на SpigotMC - число в конце ссылки на страницу плагина. */
    public static final int SPIGOT_RESOURCE_ID = 0;

    /** ID плагина на bstats.org для Bukkit/Spigot/Paper. */
    public static final int BSTATS_ID_BUKKIT = 0;

    /** ID плагина на bstats.org для Velocity (регистрируется отдельно). */
    public static final int BSTATS_ID_VELOCITY = 0;

    /** Право получать уведомления о новой версии. */
    public static final String UPDATE_PERMISSION = "skinpatchvh.update";

    private PluginInfo() {
    }
}
