package me.v0rham.skinPatchVH;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Общая логика плагина для Paper и Velocity. Все методы блокирующие -
 * вызывать только из асинхронного потока.
 */
public final class SkinManager {

    private static final Pattern NICK = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    public enum Status {OK, NOT_FOUND, ERROR}

    public record Result(Status status, SkinData data) {
    }

    private final SkinStorage storage;
    private final SkinService service = new SkinService();
    private final SkinLogger logger;

    public SkinManager(Path dataDirectory, SkinLogger logger) {
        this.logger = logger;
        this.storage = new SkinStorage(dataDirectory, logger);
    }

    public static boolean isValidNick(String nick) {
        return NICK.matcher(nick).matches();
    }

    public boolean hasStoredSkin(UUID id) {
        return storage.get(id).isPresent();
    }

    /**
     * Скин, который нужно выдать игроку при входе: сохранённый выбор, либо
     * (если у профиля нет текстур, т.е. offline-режим) скин по нику игрока.
     */
    public Optional<SkinData> resolveForLogin(UUID id, String username, boolean profileHasTextures) {
        Optional<SkinData> stored = storage.get(id);
        if (stored.isPresent() || profileHasTextures) {
            return stored;
        }
        try {
            return service.fetch(username);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Не удалось получить скин для " + username + ": " + e);
            return Optional.empty();
        }
    }

    /**
     * Смена скина игрока на скин с ником nick, либо сброс (reset = true).
     * При сбросе отсутствие скина по нику не считается ошибкой: возвращается OK
     * с data == null, что означает "скин по умолчанию".
     */
    public Result change(UUID id, String nick, boolean reset) {
        Optional<SkinData> data;
        try {
            data = service.fetch(nick);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result(Status.ERROR, null);
        } catch (Exception e) {
            logger.warn("Ошибка загрузки скина " + nick + ": " + e);
            return new Result(Status.ERROR, null);
        }

        if (reset) {
            storage.remove(id);
        } else {
            data.ifPresent(d -> storage.set(id, d));
        }
        if (data.isEmpty() && reset) {
            return new Result(Status.OK, null);
        }
        return data.map(d -> new Result(Status.OK, d))
                .orElseGet(() -> new Result(Status.NOT_FOUND, null));
    }
}
