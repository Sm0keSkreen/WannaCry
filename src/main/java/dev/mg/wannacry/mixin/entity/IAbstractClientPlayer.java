package dev.mg.wannacry.mixin.entity;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractClientPlayer.class)
public interface IAbstractClientPlayer {
    @Accessor("playerInfo")
    PlayerInfo wannacry_getPlayerInfo();

    @Accessor("playerInfo")
    void wannacry_setPlayerInfo(PlayerInfo pi);
}
