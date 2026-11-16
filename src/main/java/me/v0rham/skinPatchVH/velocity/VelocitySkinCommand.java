package me.v0rham.skinPatchVH.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.v0rham.skinPatchVH.SkinManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Velocity:
 * /skin <ник>  - сохранить скин игрока с указанным ником
 * /skin reset  - вернуть скин по своему нику
 * На прокси скин нельзя сменить "на лету", поэтому он применяется при следующем входе.
 */
public final class VelocitySkinCommand implements SimpleCommand {

    private static final String PERMISSION = "skinpatchvh.use";

    private final Object plugin;
    private final ProxyServer server;
    private final SkinManager manager;

    public VelocitySkinCommand(Object plugin, ProxyServer server, SkinManager manager) {
        this.plugin = plugin;
        this.server = server;
        this.manager = manager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Команда доступна только игрокам.", NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length != 1) {
            player.sendMessage(Component.text("Использование: /" + invocation.alias() + " <ник> | reset",
                    NamedTextColor.YELLOW));
            return;
        }

        boolean reset = args[0].equalsIgnoreCase("reset");
        String nick = reset ? player.getUsername() : args[0];

        if (!SkinManager.isValidNick(nick)) {
            player.sendMessage(Component.text("Некорректный ник.", NamedTextColor.RED));
            return;
        }

        UUID id = player.getUniqueId();
        player.sendMessage(Component.text("Загружаю скин...", NamedTextColor.GRAY));

        server.getScheduler().buildTask(plugin, () -> {
            SkinManager.Result result = manager.change(id, nick, reset);
            switch (result.status()) {
                case ERROR -> player.sendMessage(Component.text(
                        "Не удалось связаться с Mojang, попробуйте позже.", NamedTextColor.RED));
                case NOT_FOUND -> player.sendMessage(Component.text(reset
                        ? "Сохранённый скин удалён, но по вашему нику скин не найден (аккаунт Mojang с таким ником не существует)."
                        : "Скин для ника " + nick + " не найден.", NamedTextColor.RED));
                case OK -> player.sendMessage(Component.text(
                        (reset ? "Скин сброшен на ваш ник" : "Скин сохранён: " + result.data().name())
                                + ". Перезайдите на сервер, чтобы он применился.",
                        NamedTextColor.GREEN));
            }
        }).schedule();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().getPermissionValue(PERMISSION) != Tristate.FALSE;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 1) {
            return List.of();
        }
        String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        if ("reset".startsWith(prefix)) {
            out.add("reset");
        }
        for (Player p : server.getAllPlayers()) {
            if (p.getUsername().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                out.add(p.getUsername());
            }
        }
        return out;
    }
}
