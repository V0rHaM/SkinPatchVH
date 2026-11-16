package me.v0rham.skinPatchVH;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        SkinStorage storage = new SkinStorage(getDataFolder(), getLogger());
        SkinService service = new SkinService();

        getServer().getPluginManager().registerEvents(new SkinListener(storage, service, getLogger()), this);

        PluginCommand command = getCommand("skin");
        if (command != null) {
            command.setExecutor(new SkinCommand(this, storage, service));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
