package dev.mg.wannacry.mixin.render.gui;

import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.features.modules.render.NoRenderModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.mg.wannacry.util.traits.Util.EVENT_BUS;
import static dev.mg.wannacry.util.traits.Util.mc;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "render", at = @At("RETURN"))
    public void render(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        boolean debugOpen = mc.debugEntries.isOverlayVisible();
        if (debugOpen) return;

        Render2DEvent event = new Render2DEvent(context, tickCounter.getGameTimeDeltaPartialTick(true));
        EVENT_BUS.post(event);
    }

    private static boolean satellite$nr(java.util.function.Function<NoRenderModule, dev.mg.wannacry.features.settings.Setting<Boolean>> getter) {
        NoRenderModule nr = NoRenderModule.getInstance();
        return nr != null && nr.isEnabled() && getter.apply(nr).getValue();
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void satellite$noCrosshair(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.crosshair)) ci.cancel();
    }

    @Inject(method = "renderBossOverlay", at = @At("HEAD"), cancellable = true)
    private void satellite$noBossBar(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.bossBar)) ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noScoreboard(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.scoreboard)) ci.cancel();
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noTitle(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.title)) ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noPotionIcons(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.potionIcons)) ci.cancel();
    }

    @Inject(method = "renderVignette(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noVignette(GuiGraphics g, Entity entity, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.vignette)) ci.cancel();
    }

    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noHeldItemName(GuiGraphics g, CallbackInfo ci) {
        if (satellite$nr(nr -> nr.heldItemName)) ci.cancel();
    }

    @Inject(method = "renderCameraOverlays", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noCameraOverlays(GuiGraphics g, DeltaTracker dt, CallbackInfo ci) {
        NoRenderModule nr = NoRenderModule.getInstance();
        if (nr == null || !nr.isEnabled()) return;
        if (mc.player == null) return;

        Player p = mc.player;

        if (nr.fireOverlay.getValue()        && p.isOnFire())                              { ci.cancel(); return; }
        if (nr.liquidOverlay.getValue()       && (p.isEyeInFluid(FluidTags.WATER)
                                                || p.isEyeInFluid(FluidTags.LAVA)))       { ci.cancel(); return; }
        if (nr.portalOverlay.getValue()       && p.getPortalCooldown() > 0)               { ci.cancel(); return; }
        if (nr.spyglassOverlay.getValue()     && p.isScoping())                           { ci.cancel(); return; }
        if (nr.pumpkinOverlay.getValue()      && hasPumpkinHelmet(p))                     { ci.cancel(); return; }
        if (nr.powderedSnowOverlay.getValue() && p.isInPowderSnow)                        { ci.cancel(); return; }
        if (nr.inWallOverlay.getValue()       && p.isInWall())                            { ci.cancel(); return; }
    }

    private static boolean hasPumpkinHelmet(Player p) {
        net.minecraft.world.item.ItemStack helm = p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        return !helm.isEmpty() && helm.is(net.minecraft.world.item.Items.CARVED_PUMPKIN);
    }
}
