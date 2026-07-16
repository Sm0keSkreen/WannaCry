package dev.mg.wannacry.features.modules.player;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.entity.player.TickEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.modules.exploit.XCarryModule;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.inventory.InventoryUtil;
import dev.mg.wannacry.util.inventory.Result;
import dev.mg.wannacry.util.inventory.ResultType;
import dev.mg.wannacry.util.models.Timer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TotemModule extends Module {

    public enum Mode {
        FULL,
        INVENTORY,
        HOVER
    }

    private final Setting<Mode>    mode          = mode("Mode",           Mode.FULL);

    private final Setting<Float>   delayMs       = num( "DelayMs",        50f, 0f, 500f);

    private final Setting<Boolean> autoOpen      = bool("AutoOpen",       false);

    private final Setting<Boolean> autoClose     = bool("AutoClose",      false);

    private final Setting<Boolean> hotbarFill    = bool("HotbarFill",     false);

    private final Setting<Boolean> xcarryCrafting = bool("XCarryCrafting", true);

    private final Timer fullTimer = new Timer();

    private boolean autoOpenedInventory = false;

    private boolean justPlacedTotem = false;

    public TotemModule() {
        super("Totem",
              "Helps with toteming quickly. Full: auto totem. " +
              "Inventory: auto totem only while the inventory is open. " +
              "Hover: offhands/hotbar fills instantly when hovered over. " +
              "XCarry: also pulls totems from the crafting grid when XCarry is active.",
              Category.CLOSET);

        delayMs       .setVisibility(v -> mode.getValue() == Mode.FULL || mode.getValue() == Mode.INVENTORY);
        autoOpen      .setVisibility(v -> mode.getValue() == Mode.INVENTORY);
        autoClose     .setVisibility(v -> mode.getValue() == Mode.INVENTORY);
        hotbarFill    .setVisibility(v -> mode.getValue() == Mode.HOVER);
        xcarryCrafting.setVisibility(v -> mode.getValue() == Mode.FULL || mode.getValue() == Mode.INVENTORY);
    }

    @Override
    public void onEnable() {
        fullTimer.reset();
        autoOpenedInventory = false;
        justPlacedTotem     = false;
    }

    @Override
    public void onDisable() {

        if (autoOpenedInventory && mc.player != null && mc.screen instanceof InventoryScreen) {
            mc.player.closeContainer();
        }
        autoOpenedInventory = false;
        justPlacedTotem     = false;
    }

    @Subscribe
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        switch (mode.getValue()) {
            case FULL, INVENTORY -> {
                if (!fullTimer.passedMs(delayMs.getValue().longValue())) return;
                fullTimer.reset();
                if (mode.getValue() == Mode.FULL) tickFull();
                else                              tickInventory();
            }
            case HOVER -> tickHover();
        }
    }

    private void tickFull() {
        if (mc.screen != null && !(mc.screen instanceof InventoryScreen)) return;
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        Result result = InventoryUtil.find(Items.TOTEM_OF_UNDYING, InventoryUtil.FULL_SCOPE);
        if (result.found() && result.type() != ResultType.OFFHAND) {
            InventoryUtil.click(toContainerSlot(result), 40, ClickType.SWAP);
            return;
        }

        if (xcarryCrafting.getValue() && isXCarryEnabled()) {
            int craftingSlot = findTotemInCraftingGrid();
            if (craftingSlot != -1) {
                InventoryUtil.click(craftingSlot, 40, ClickType.SWAP);
            }
        }
    }

    private void tickInventory() {

        if (mc.screen != null && !(mc.screen instanceof InventoryScreen)) return;

        boolean totemInOffhand = mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);

        if (justPlacedTotem && totemInOffhand) {
            justPlacedTotem     = false;
            autoOpenedInventory = false;
            if (autoClose.getValue() && mc.screen instanceof InventoryScreen) {
                mc.player.closeContainer();
            }
            return;
        }

        if (totemInOffhand) {
            justPlacedTotem     = false;
            autoOpenedInventory = false;
            return;
        }

        if (justPlacedTotem) return;

        Result result = InventoryUtil.find(Items.TOTEM_OF_UNDYING, InventoryUtil.INVENTORY_SCOPE);
        if (!result.found() || result.type() == ResultType.OFFHAND) {
            result = InventoryUtil.find(Items.TOTEM_OF_UNDYING, InventoryUtil.HOTBAR_SCOPE);
        }

        boolean foundInNormalSlots = result.found() && result.type() != ResultType.OFFHAND;

        int craftingSlot = -1;
        if (!foundInNormalSlots && xcarryCrafting.getValue() && isXCarryEnabled()) {
            craftingSlot = findTotemInCraftingGrid();
        }

        if (!foundInNormalSlots && craftingSlot == -1) {

            return;
        }

        if (!(mc.screen instanceof InventoryScreen)) {

            if (autoOpen.getValue() && !autoOpenedInventory) {
                mc.setScreen(new InventoryScreen(mc.player));
                autoOpenedInventory = true;
            }
            return;
        }

        if (foundInNormalSlots) {
            InventoryUtil.click(toContainerSlot(result), 40, ClickType.SWAP);
        } else {

            InventoryUtil.click(craftingSlot, 40, ClickType.SWAP);
        }
        justPlacedTotem = true;
    }

    private void tickHover() {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        Slot hoveredSlot = screen.hoveredSlot;
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;
        if (!hoveredSlot.getItem().is(Items.TOTEM_OF_UNDYING)) return;

        if (!mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {

            InventoryUtil.click(hoveredSlot.index, 40, ClickType.SWAP);
            return;
        }

        if (!hotbarFill.getValue()) return;

        if (hoveredSlot.index == 45) return;

        if (hoveredSlot.index >= 36 && hoveredSlot.index <= 44) return;

        int emptySlot = findEmptyHotbarSlot();
        if (emptySlot == -1) return;

        InventoryUtil.click(hoveredSlot.index, emptySlot, ClickType.SWAP);
    }

    private int findTotemInCraftingGrid() {

        for (int containerSlot = 1; containerSlot <= 4; containerSlot++) {
            net.minecraft.world.inventory.Slot slot =
                    mc.player.inventoryMenu.getSlot(containerSlot);
            if (slot != null && slot.hasItem()
                    && slot.getItem().is(Items.TOTEM_OF_UNDYING)) {
                return containerSlot;
            }
        }
        return -1;
    }

    private boolean isXCarryEnabled() {
        XCarryModule xcarry = WannaCry.moduleManager.getModuleByClass(XCarryModule.class);
        return xcarry != null && xcarry.isEnabled();
    }

    private int findEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) return i;
        }
        return -1;
    }

    private static int toContainerSlot(Result result) {
        return result.type() == ResultType.HOTBAR ? result.slot() + 36 : result.slot();
    }

    @Override
    public String getDisplayInfo() {
        return switch (mode.getValue()) {
            case FULL     -> "Full";
            case INVENTORY -> {
                if (autoOpen.getValue() && autoClose.getValue()) yield "Inv+AC";
                if (autoOpen.getValue())  yield "Inv+Open";
                if (autoClose.getValue()) yield "Inv+Close";
                yield "Inventory";
            }
            case HOVER    -> hotbarFill.getValue() ? "Hover+Fill" : "Hover";
        };
    }
}
