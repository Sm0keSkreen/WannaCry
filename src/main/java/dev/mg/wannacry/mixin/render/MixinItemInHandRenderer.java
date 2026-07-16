package dev.mg.wannacry.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.mg.wannacry.features.modules.render.ViewModelModule;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Unique
    private boolean satellite$pushed = false;

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void satellite$vmHead(
            LivingEntity entity,
            ItemStack item,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        satellite$pushed = false;

        InteractionHand hand;
        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            hand = InteractionHand.MAIN_HAND;
        } else if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            hand = InteractionHand.OFF_HAND;
        } else {
            return;
        }

        ViewModelModule m = ViewModelModule.getInstance();
        if (m == null || !m.isEnabled()) return;

        poseStack.pushPose();
        poseStack.translate(m.getPosX(hand), m.getPosY(hand), m.getPosZ(hand));
        poseStack.mulPose(Axis.XP.rotationDegrees(m.getRotX(hand)));
        poseStack.mulPose(Axis.YP.rotationDegrees(m.getRotY(hand)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(m.getRotZ(hand)));
        poseStack.scale(m.getScale(hand), m.getScale(hand), m.getScale(hand));
        satellite$pushed = true;
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void satellite$vmTail(
            LivingEntity entity,
            ItemStack item,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        if (satellite$pushed) {
            poseStack.popPose();
            satellite$pushed = false;
        }
    }
}
