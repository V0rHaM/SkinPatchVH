package me.v0rham.skinPatchVH.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.util.GameProfile;
import me.v0rham.skinPatchVH.SkinManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Velocity: подставляет скин в профиль игрока при входе на прокси.
 */
public final class VelocitySkinListener {

    private final SkinManager manager;

    public VelocitySkinListener(SkinManager manager) {
        this.manager = manager;
    }

    @Subscribe(order = PostOrder.LATE)
    public EventTask onGameProfileRequest(GameProfileRequestEvent event) {
        return EventTask.async(() -> {
            GameProfile profile = event.getGameProfile();
            boolean hasTextures = profile.getProperties().stream()
                    .anyMatch(p -> "textures".equals(p.getName()));

            manager.resolveForLogin(profile.getId(), event.getUsername(), hasTextures).ifPresent(data -> {
                List<GameProfile.Property> properties = new ArrayList<>();
                for (GameProfile.Property property : profile.getProperties()) {
                    if (!"textures".equals(property.getName())) {
                        properties.add(property);
                    }
                }
                properties.add(new GameProfile.Property(
                        "textures", data.value(), data.signature() == null ? "" : data.signature()));
                event.setGameProfile(profile.withProperties(properties));
            });
        });
    }
}
