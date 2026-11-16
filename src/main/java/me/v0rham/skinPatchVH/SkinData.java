package me.v0rham.skinPatchVH;

import org.jetbrains.annotations.Nullable;

/**
 * Текстура скина: ник-источник, значение и подпись (подпись может отсутствовать).
 */
public record SkinData(String name, String value, @Nullable String signature) {
}
