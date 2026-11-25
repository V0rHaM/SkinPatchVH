package me.v0rham.skinPatchVH;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.entity.Player;

/**
 * Paper (и форки): применение скина через PlayerProfile API.
 */
public final class PaperSkinApplier implements SkinApplier {

    @Override
    public Result apply(Player player, SkinData data) {
        PlayerProfile profile = player.getPlayerProfile();
        if (data == null) {
            profile.removeProperty("textures");
        } else {
            profile.setProperty(new ProfileProperty("textures", data.value(), data.signature()));
        }
        player.setPlayerProfile(profile);
        return Result.APPLIED;
    }
}
