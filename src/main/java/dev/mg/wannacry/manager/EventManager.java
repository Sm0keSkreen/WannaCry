package dev.mg.wannacry.manager;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.Stage;
import dev.mg.wannacry.event.impl.entity.DeathEvent;
import dev.mg.wannacry.event.impl.entity.player.TickEvent;
import dev.mg.wannacry.event.impl.entity.player.UpdateWalkingPlayerEvent;
import dev.mg.wannacry.event.impl.input.KeyInputEvent;
import dev.mg.wannacry.event.impl.input.MouseInputEvent;
import dev.mg.wannacry.event.impl.network.ChatEvent;
import dev.mg.wannacry.event.impl.network.PacketEvent;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.event.impl.render.Render3DEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.Feature;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.entity.player.Player;

public class EventManager extends Feature {
    public void init() {
        EVENT_BUS.register(this);
    }

    public void onUnload() {
        EVENT_BUS.unregister(this);
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        if (nullCheck())
            return;
        WannaCry.moduleManager.onTick();
        for (Player player : mc.level.players()) {
            if (player == null || player.getHealth() > 0.0F)
                continue;
            EVENT_BUS.post(new DeathEvent(player));
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        WannaCry.serverManager.onPacketReceived();
        if (event.getPacket() instanceof ClientboundSetTimePacket)
            WannaCry.serverManager.update();
        if (event.getPacket() instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload)
                && payload instanceof BrandPayload(String brand)) {
            WannaCry.serverManager.setServerBrand(brand);
        }
    }

    @Subscribe
    public void onWorldRender(Render3DEvent event) {
        WannaCry.moduleManager.onRender3D(event);
    }

    @Subscribe
    public void onRenderGameOverlayEvent(Render2DEvent event) {
        WannaCry.moduleManager.onRender2D(event);
    }

    @Subscribe
    public void onKeyInput(KeyInputEvent event) {
        WannaCry.moduleManager.onKeyPressed(event.getKey());
    }

    @Subscribe
    public void onMouseInput(MouseInputEvent event) {
        if (event.getAction() == 1) {
            WannaCry.moduleManager.onMouseClicked(event.getButton());
        }
    }

    @Subscribe
    public void onChatSent(ChatEvent event) {
        String message = event.getMessage();
        if (!message.startsWith(WannaCry.commandManager.getCommandPrefix())) {
            return;
        }
        event.cancel();
        WannaCry.commandManager.onChatSent(message);
    }
}
