package me.v0rham.skinPatchVH;

/**
 * Минимальный интерфейс логирования, чтобы общий код не зависел от платформы
 * (Paper использует java.util.logging, Velocity - slf4j).
 */
public interface SkinLogger {

    void info(String message);

    void warn(String message);

    void error(String message, Throwable throwable);
}
