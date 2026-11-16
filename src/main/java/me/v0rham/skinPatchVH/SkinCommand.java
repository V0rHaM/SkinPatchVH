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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * /skin <ник>  - установить скин игрока с указанным ником
 * /skin reset  - вернуть скин по своему нику
 */
public final class SkinCommand implements TabExecutor {

    private static final Pattern NICK = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    private final JavaPlugin plugin;
    private final SkinStorage storage;
    private final SkinService service;

    public SkinCommand(JavaPlugin plugin, SkinStorage storage, SkinService service) {
        this.plugin = plugin;
        this.storage = storage;
        this.service = service;
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

        if (!NICK.matcher(nick).matches()) {
            player.sendMessage(Component.text("Некорректный ник.", NamedTextColor.RED));
            return true;
        }

        UUID id = player.getUniqueId();
        player.sendMessage(Component.text("Загружаю скин...", NamedTextColor.GRAY));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<SkinData> result;
            try {
                result = service.fetch(nick);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка загрузки скина " + nick + ": " + e.getMessage());
                result = Optional.empty();
            }

            Optional<SkinData> finalResult = result;
            if (finalResult.isPresent()) {
                if (reset) {
                    storage.remove(id);
                } else {
                    storage.set(id, finalResult.get());
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player online = Bukkit.getPlayer(id);
                if (online == null) {
                    return;
                }
                if (finalResult.isEmpty()) {
                    online.sendMessage(Component.text("Скин для ника " + nick + " не найден.", NamedTextColor.RED));
                    return;
                }
                apply(online, finalResult.get());
                online.sendMessage(Component.text(
                        reset ? "Скин сброшен на ваш ник." : "Скин изменён на " + finalResult.get().name() + ".",
                        NamedTextColor.GREEN));
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
