package dev.mg.wannacry.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ShieldUtils {

    private ShieldUtils() {}

    public static boolean isShielding(Player target) {
        if (!target.isUsingItem()) return false;

        InteractionHand hand = target.getUsedItemHand();
        ItemStack stack = target.getItemInHand(hand);
        if (stack.isEmpty()) return false;

        if (stack.get(DataComponents.BLOCKS_ATTACKS) == null) return false;

        return isFacingUs(target);
    }

    public static boolean isShieldingAny(Player target) {
        if (!target.isUsingItem()) return false;
        InteractionHand hand = target.getUsedItemHand();
        ItemStack stack = target.getItemInHand(hand);
        if (stack.isEmpty()) return false;
        return stack.get(DataComponents.BLOCKS_ATTACKS) != null;
    }

    public static boolean isFacingUs(Player target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        Vec3 targetToUs = mc.player.position().subtract(target.position());
        double len = targetToUs.length();
        if (len < 1e-6) return true;
        targetToUs = targetToUs.scale(1.0 / len);

        double yawRad = Math.toRadians(target.getYRot());
        double fwdX = -Math.sin(yawRad);
        double fwdZ =  Math.cos(yawRad);

        double dot = fwdX * targetToUs.x + fwdZ * targetToUs.z;
        return dot > 0.0;
    }
}
