package me.v0rham.skinPatchVH;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Применение скина к игроку, который уже находится на сервере.
 * Реализация зависит от платформы (Paper или Spigot).
 */
public interface SkinApplier {

    enum Result {
        /** Скин применён сразу. */
        APPLIED,
        /** Скин применён, но сам игрок увидит его только после перезахода. */
        RELOG_REQUIRED,
        /** Применить на лету не удалось (скин всё равно сохранён и применится при входе). */
        FAILED
    }

    /**
     * @param data скин или null, чтобы вернуть скин по умолчанию (убрать текстуры)
     */
    Result apply(Player player, @Nullable SkinData data);
}
