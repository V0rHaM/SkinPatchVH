package me.v0rham.skinPatchVH;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Paper:
 * /skin <ник>  - установить скин игрока с указанным ником
 * /skin reset  - вернуть скин по своему нику
 */
public final class SkinCommand implements TabExecutor {

    private final JavaPlugin plugin;
    private final SkinManager manager;

    public SkinCommand(JavaPlugin plugin, SkinManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Команда доступна только игрокам.", NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(Component.text("Использование: /" + label + " <ник> | reset", NamedTextColor.YELLOW));
            return true;
        }

        boolean reset = args[0].equalsIgnoreCase("reset");
        String nick = reset ? player.getName() : args[0];

        if (!SkinManager.isValidNick(nick)) {
            player.sendMessage(Component.text("Некорректный ник.", NamedTextColor.RED));
            return true;
        }

        UUID id = player.getUniqueId();
        player.sendMessage(Component.text("Загружаю скин...", NamedTextColor.GRAY));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            SkinManager.Result result = manager.change(id, nick, reset);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player online = Bukkit.getPlayer(id);
                if (online == null) {
                    return;
                }
                switch (result.status()) {
                    case ERROR -> online.sendMessage(Component.text(
                            "Не удалось связаться с Mojang, попробуйте позже.", NamedTextColor.RED));
                    case NOT_FOUND -> online.sendMessage(Component.text(reset
                            ? "Сохранённый скин удалён, но по вашему нику скин не найден (аккаунт Mojang с таким ником не существует)."
                            : "Скин для ника " + nick + " не найден.", NamedTextColor.RED));
                    case OK -> {
                        apply(online, result.data());
                        online.sendMessage(Component.text(
                                reset ? "Скин сброшен на ваш ник." : "Скин изменён на " + result.data().name() + ".",
                                NamedTextColor.GREEN));
                    }
                }
            });
        });
        return true;
    }

    private void apply(Player player, SkinData data) {
        PlayerProfile profile = player.getPlayerProfile();
        profile.setProperty(new ProfileProperty("textures", data.value(), data.signature()));
        player.setPlayerProfile(profile);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        if ("reset".startsWith(prefix)) {
            out.add("reset");
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                out.add(p.getName());
            }
        }
        return out;
    }
}
