package me.v0rham.skinPatchVH;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * /skin <ник>  - установить скин игрока с указанным ником
 * /skin reset  - вернуть скин по своему нику
 * Не использует Paper-специфичный API, поэтому работает и на Spigot.
 */
public final class SkinCommand implements TabExecutor {

    private final JavaPlugin plugin;
    private final SkinManager manager;
    private final SkinApplier applier;

    public SkinCommand(JavaPlugin plugin, SkinManager manager, SkinApplier applier) {
        this.plugin = plugin;
        this.manager = manager;
        this.applier = applier;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Команда доступна только игрокам.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Использование: /" + label + " <ник> | reset");
            return true;
        }

        boolean reset = args[0].equalsIgnoreCase("reset");
        String nick = reset ? player.getName() : args[0];

        if (!SkinManager.isValidNick(nick)) {
            player.sendMessage(ChatColor.RED + "Некорректный ник.");
            return true;
        }

        UUID id = player.getUniqueId();
        player.sendMessage(ChatColor.GRAY + "Загружаю скин...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            SkinManager.Result result = manager.change(id, nick, reset);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Player online = Bukkit.getPlayer(id);
                if (online == null) {
                    return;
                }
                switch (result.status()) {
                    case ERROR:
                        online.sendMessage(ChatColor.RED + "Не удалось связаться с Mojang, попробуйте позже.");
                        break;
                    case NOT_FOUND:
                        online.sendMessage(ChatColor.RED + "Скин для ника " + nick + " не найден.");
                        break;
                    case OK:
                        reportApplied(online, result.data(), reset);
                        break;
                }
            });
        });
        return true;
    }

    private void reportApplied(Player player, SkinData data, boolean reset) {
        String done = reset ? "Скин сброшен." : "Скин изменён на " + data.name() + ".";
        switch (applier.apply(player, data)) {
            case APPLIED:
                player.sendMessage(ChatColor.GREEN + done);
                break;
            case RELOG_REQUIRED:
                player.sendMessage(ChatColor.GREEN + done + " Перезайдите, чтобы увидеть новый скин.");
                break;
            case FAILED:
                player.sendMessage(ChatColor.YELLOW + "Скин сохранён и будет применён при следующем входе на сервер.");
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
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
