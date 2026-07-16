package dev.mg.wannacry.features.modules.combat;

import dev.mg.wannacry.event.impl.input.AttackEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.EnchantmentUtil;
import dev.mg.wannacry.util.inventory.InventoryUtil;
import dev.mg.wannacry.util.inventory.Result;
import dev.mg.wannacry.util.inventory.ResultType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.HitResult;

import java.util.EnumSet;

public class MaceSpoofModule extends Module {

    private static final ResourceKey<Enchantment> BREACH =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("minecraft:breach"));

    private static final ResourceKey<Enchantment> DENSITY =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("minecraft:density"));

    private static final EnumSet<ResultType> HOTBAR_ONLY =
            EnumSet.of(ResultType.HOTBAR);

    private final Setting<Float> height = num(
            "SpoofHeight", 20.0f, 1.0f, 50.0f);

    private final Setting<Boolean> dtap = bool("DTap", false);

    private final Setting<Boolean> anySlot = bool("AnySlot", true);

    private final Setting<Integer> breachSlot = register(
            new Setting<>("BreachSlot", 1, 1, 9, v -> !anySlot.getValue()));

    private final Setting<Integer> densitySlot = register(
            new Setting<>("DensitySlot", 2, 1, 9, v -> !anySlot.getValue()));

    private Entity dtapTarget      = null;

    private Result dtapSecondMace  = null;

    private int    dtapSavedSlot   = -1;

    public MaceSpoofModule() {
        super("MaceSpoof",
              "Spoofs your fall height to instantly mace smash.",
              Category.EXPLOIT);
    }

    @Override
    public void onDisable() {
        clearDTapState();
    }

    @Subscribe
    public void onAttackPre(AttackEvent.Pre event) {
        if (nullCheck()) return;

        boolean entityTargeted = (mc.hitResult != null
                && mc.hitResult.getType() == HitResult.Type.ENTITY)
                || TriggerBotModule.overrideTarget != null;
        if (!entityTargeted) return;

        if (dtap.getValue()) {
            handleDTapPre();
        } else {

            if (mc.player.getMainHandItem().is(Items.MACE)) {
                sendSpoof();
            }
        }
    }

    @Subscribe
    public void onAttackPost(AttackEvent.Post event) {
        if (nullCheck()) return;
        if (!dtap.getValue()) return;
        if (dtapTarget == null || dtapSecondMace == null) return;

        Entity target     = dtapTarget;
        Result secondMace = dtapSecondMace;
        int    savedSlot  = dtapSavedSlot;
        clearDTapState();

        if (target.isRemoved() || !target.isAlive()) {

            InventoryUtil.swap(savedSlot);
            return;
        }

        InventoryUtil.swap(secondMace);
        sendSpoof();
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        InventoryUtil.swapBack(secondMace, savedSlot);
    }

    private void handleDTapPre() {
        Result breachMace;
        Result densityMace;

        if (anySlot.getValue()) {

            breachMace = InventoryUtil.find(
                    s -> !s.isEmpty() && s.is(Items.MACE) && EnchantmentUtil.has(BREACH, s),
                    HOTBAR_ONLY);
            densityMace = InventoryUtil.find(
                    s -> !s.isEmpty() && s.is(Items.MACE) && EnchantmentUtil.has(DENSITY, s),
                    HOTBAR_ONLY);
        } else {

            int bSlot = breachSlot.getValue() - 1;
            int dSlot = densitySlot.getValue() - 1;

            var bStack = mc.player.getInventory().getItem(bSlot);
            var dStack = mc.player.getInventory().getItem(dSlot);

            breachMace = (!bStack.isEmpty() && bStack.is(Items.MACE) && EnchantmentUtil.has(BREACH, bStack))
                    ? new Result(bSlot, bStack, ResultType.HOTBAR)
                    : new Result(-1, net.minecraft.world.item.ItemStack.EMPTY, ResultType.NONE);

            densityMace = (!dStack.isEmpty() && dStack.is(Items.MACE) && EnchantmentUtil.has(DENSITY, dStack))
                    ? new Result(dSlot, dStack, ResultType.HOTBAR)
                    : new Result(-1, net.minecraft.world.item.ItemStack.EMPTY, ResultType.NONE);
        }

        if (!breachMace.found() || !densityMace.found()
                || breachMace.slot() == densityMace.slot()) {

            if (mc.player.getMainHandItem().is(Items.MACE)) sendSpoof();
            return;
        }

        Result firstMace  = breachMace;
        Result secondMace = densityMace;

        dtapSavedSlot  = InventoryUtil.selected();
        dtapTarget     = TriggerBotModule.overrideTarget != null
                ? TriggerBotModule.overrideTarget
                : mc.crosshairPickEntity;
        dtapSecondMace = secondMace;

        if (!firstMace.holding()) {
            InventoryUtil.swap(firstMace);
        }

        sendSpoof();
    }

    private void sendSpoof() {
        float h = height.getValue();

        for (int i = 0; i < 3; i++) {
            sendFakeY(0, false);
        }

        sendFakeY(h, false);

        sendFakeY(0, false);
    }

    private void sendFakeY(double offset, boolean onGround) {
        mc.player.connection.send(
                new ServerboundMovePlayerPacket.Pos(
                        mc.player.getX(),
                        mc.player.getY() + offset,
                        mc.player.getZ(),
                        onGround,
                        mc.player.horizontalCollision));
    }

    private void clearDTapState() {
        dtapTarget     = null;
        dtapSecondMace = null;
        dtapSavedSlot  = -1;
    }
}
