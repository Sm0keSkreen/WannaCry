package dev.mg.wannacry.mixin.entity;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInfo.class)
public interface IPlayerInfo {
    @Accessor("gameMode")
    GameType wannacry_getGameMode();

    @Accessor("gameMode")
    void wannacry_setGameMode(GameType mode);
}
